package de.uma.dws.graphsm.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;

public class TextFile {
	
	public static void main(String[] args) throws IOException {
		
		String input = "src/main/resources/dataset/LeePincombe/documents.cleaned.data";
		String output = "src/main/resources/dataset/LeePincombe/documents.cleaned.allpairs";
		
		FileWriter out = new FileWriter(output);
	   
		ArrayList<String> docs = MyFileReader.readLinesFile(new File(input));
		
		int  size = docs.size();
		
		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				out.write(docs.get(i).trim());
				out.write("\t");
				out.write(docs.get(j).trim());
				out.write("\n");
			}
		}
		
		out.close();
		
   }

}
