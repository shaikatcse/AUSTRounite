package aust.cse.routine.analyseResults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class MergeAllParetofronts {
	
	static PrintWriter writer; 


	public static void traveresDirectory(File[] files) {
	    for (File file : files) {
	        if (file.isDirectory()) {
	            //System.out.println("Directory: " + file.getName());
	            traveresDirectory(file.listFiles()); // Calls same method again.
	        } else {
	        	if(file.getName().equals("FIS_FUN_NSGAII") &&
	        			file.length() != 0) {
	        	readAndMerge(file.getAbsolutePath());
	        	//System.out.println("File: " + file.getName());
	        	}
	        	}
	    }
	}
	
	static void readAndMerge(String fileName){
		try {
			
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    String everything = sb.toString();
		    writer.write(everything);
		    br.close();
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	
	public static void main(String[] args) {
		try {
			File[] files = new File(args[0]).listFiles();
			writer = new PrintWriter(args[0]+"\\mergeFronts", "UTF-8");
			traveresDirectory(files);
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
