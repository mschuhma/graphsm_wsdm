package de.uma.dws.graphsm.webservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;

public class TagMe implements Annotator{

	final static Configuration	conf	= ConfFactory.getConf();
	final static Logger	      	log	= LoggerFactory.getLogger(TagMe.class);

	final static String	      TagMeUrl	= conf.getString("tagme.url");
	final static String	      TagMeKey	= conf.getString("tagme.api.key");

	@SuppressWarnings("unchecked")
	public ArrayList<String> annotate(String text) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("key", TagMeKey));
		params.add(new BasicNameValuePair("text", text));

		// params.add(new BasicNameValuePair("lang", "de")); //default language is "en"
		// params.add(new BasicNameValuePair("tweet", "true")); //to enable special parser for Twitter messages
		// params.add(new BasicNameValuePair("include_abstract", "true")); //to enable the inclusion of related Wiki pages
		// in case of disambiguated spots
		// params.add(new BasicNameValuePair("include_categories", "true")); //to enable the inclusion of a list of
		// categories from DBpedia
		// params.add(new BasicNameValuePair("include_all_spots", "true")); //to enable the inclusion of all spots
		// including those ones TAGME was not able to annotate with a topic

		// params.add(new BasicNameValuePair("long_text", "0")); //advanced option for long texts (0: annotate the whole
		// text; int > 0 to annotate special amount of text)
		// params.add(new BasicNameValuePair("epsilon", "0.5")); //advanced option for finely tuning the disambiguation
		// process: an higher value will favor the most-common topics for a spot (floats out of [0 ; 0.5] - default: 0.3)

		HttpPost httppost = new HttpPost(TagMeUrl);
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("content-type", "application/x-www-form-urlencoded");

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HashMap<String, Object> jsonResult = null;

		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			httppost.setEntity(entity);
			HttpResponse res = httpclient.execute(httppost);
			log.debug("Connection status: {}", res);

			TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};
			jsonResult = new ObjectMapper().readValue(res.getEntity().getContent(), typeRef);
		}
		catch (ClientProtocolException e) {
			log.warn("TagMe Annotation Service connection error: {}", e);
		}
		catch (IOException e) {
			log.error("{}", e);
			return new ArrayList<String>();
		}
		finally {
			httppost.releaseConnection();
		}

		ArrayList<String> uriList = new ArrayList<String>(10);
		ArrayList<String> annotationList = new ArrayList<String>();
		Object res = jsonResult.get("annotations");

		if (res == null) {
			return new ArrayList<String>();
		}
		
		//p looks like {id=30636, title=Terrorism, start=257, rho=0.22990, end=266, spot=terrorism}
		for (HashMap<String, String> p : (ArrayList<HashMap<String, String>>) res) {
			if (log.isDebugEnabled()) {
				annotationList.addAll(Arrays.asList(p.get("spot")));
			}
			try {
	         uriList.add("http://dbpedia.org/resource/" + p.get("title").replace(" ", "_"));
         }
         catch (Exception e) {
         	
         	if (p.get("spot").equals("Sept. 6")) {
         		log.debug("Known TagMe bug occured: {}", p);
         	}
         	else {
         		e.printStackTrace();
         	}  
         }
		}
		if (log.isDebugEnabled()) {
			log.debug("Input: {}", text);

			ArrayList<String> textTokens = new ArrayList<String>(Arrays.asList(text.split(" ")));
			ArrayList<String> uriListDebug = new ArrayList<String>();

			for (String s : uriList) {
				uriListDebug.add(s.substring(s.lastIndexOf("/") + 1));
			}
			log.debug("Output: {}", uriListDebug);
			log.debug("Surface forms: {}", annotationList);
			
			int surfaceFormTokens = 0;
			for (String s : annotationList)
				surfaceFormTokens += s.split(" ").length;
				
			log.debug("Input text {} tokens, Dbpedia concepts {}, annotated surface forms ratio (token based) {} ", 
						textTokens.size(),
			         annotationList.size(), 
			         1.0 * surfaceFormTokens / textTokens.size());
		}
		return uriList;
	}


	@Override
   public String toString() {
	   return "TagMe";
   }


	public static void main(String[] args) {
		
		File f = new File("src/main/resources/dataset/NewsObamaBerlin/newspaper_all_text.txt");
		ArrayList<String> line = MyFileReader.readLinesFile(f);

		for (String s : line) {
			try {
				if (s == null) {
					System.out.println("Snippet empty");
					continue;
				}
				;
				ArrayList<String> tmp = new TagMe().annotate(s);
				StringBuffer bf = new StringBuffer();
				// bf.append("Input: " + s + "\nOutput: ");
				for (String t : new HashSet<String>(tmp)) {
					// bf.append(t.substring(t.lastIndexOf("/")+1) + ", ");
					bf.append(t.trim() + "\t");
				}
				System.out.println(bf);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
