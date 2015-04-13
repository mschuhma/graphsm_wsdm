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

public class DBPediaSpotlight implements Annotator{
	
	final static Logger log = LoggerFactory.getLogger(DBPediaSpotlight.class);
	final static Configuration conf = ConfFactory.getConf();
	final static String dbPediaSpotlightUrl = conf.getString("dbpedia.spotlight.url");
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> annotate(String text) {
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("disambiguator", "Default")); //Document //Default
		//params.add(new BasicNameValuePair("spotter", "AtLeastOneNounSelector")); //LingPipeSpotter (=all), CoOccurrenceBasedSelector
		//TODO Read confidence, support, disambiguator from conf file
		params.add(new BasicNameValuePair("confidence", "0.2"));
		params.add(new BasicNameValuePair("support", "3"));
		params.add(new BasicNameValuePair("text", text));
		//params.add(new BasicNameValuePair("types", "")); //Person,Organisation
		//params.add(new BasicNameValuePair("policy", "whitelist"));
		
		HttpPost httppost = new HttpPost(dbPediaSpotlightUrl);
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("content-type", "application/x-www-form-urlencoded");
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HashMap<String,Object> jsonResult = null;
		
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			httppost.setEntity(entity);
			HttpResponse res = httpclient.execute(httppost);
			log.debug("Connection status: {}", res);
		    TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
		    jsonResult = new ObjectMapper().readValue(res.getEntity().getContent(), typeRef);
		} catch (ClientProtocolException e) {
			log.warn("DBPedia Spotlight connection error: {}", e);
		} catch (IOException e) {
			log.error("{}",e);
			return new ArrayList<String>();
			}
		finally {
			httppost.releaseConnection();
			}
		
		ArrayList<String> uriList = new ArrayList<String>(10);
		ArrayList<String> annotationList = new ArrayList<String>();
		Object res = jsonResult.get("Resources");
		if (res == null)
			return new ArrayList<String>();
	    for (HashMap<String,String> p : (ArrayList<HashMap<String,String>>) res) {
	    	if (log.isDebugEnabled())
	    		annotationList.addAll(Arrays.asList(p.get("@surfaceForm").split(" ")));
	    	uriList.add(p.get("@URI"));
	    	}
	    if (log.isDebugEnabled()) {
	    	ArrayList<String> textTokens = new ArrayList<String>(Arrays.asList(text.split(" ")));
	    	log.debug("Input: {}", text);
	    	ArrayList<String> uriListDebug = new ArrayList<String>();
	    	for (String s: uriList) {
	    		uriListDebug.add(s.substring(s.lastIndexOf("/")+1));
	    	}
	    	log.debug("Output: {}", uriListDebug);
	    	log.debug("Surface forms: {}", annotationList);
		    log.debug("Input text {} tokens, annotation {} tokens, annotation ratio {} ", 
		    		textTokens.size(), annotationList.size(), 1.0*annotationList.size()/textTokens.size());
	    }  
	    return uriList;
	}

//	private DBPediaSpotlight() {}
	
	@Override
   public String toString() {
	   return "Spotlight";
   }

	public static void main(String[] args) {
		File  f = new File("src/main/resources/dataset/NewsObamaBerlin/newspaper_all_text.txt");
		ArrayList<String> line = MyFileReader.readLinesFile(f);
//		RunMetadata devRun = new RunMetadata().loadQueries();
//		ArrayList<String> line = new ArrayList<String>();
//		for (Snippet s: devRun.getQuery(0).getSnippets()) {
//			line.add(s.getTitle() + " " + s.getText());
//		}
//		
		for (String s : line) {
			try {
				if (s == null) {
					System.out.println("Snippet empty");
					continue;
				};
				ArrayList<String> tmp = new DBPediaSpotlight().annotate(s);
				StringBuffer bf = new StringBuffer();
//				bf.append("Input: " + s + "\nOutput: ");
				for (String t : new HashSet<String>(tmp)) {
//					bf.append(t.substring(t.lastIndexOf("/")+1) + ", ");
					bf.append(t.trim() + "\t");
				}
				System.out.println(bf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
