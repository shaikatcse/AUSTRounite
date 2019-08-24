package aust.cse.routine.analyseResults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CalculateGenerationNumberWhenHardConstraint {
	
	//static PrintWriter writer; 
	static int sumOfGenerationWhenFirstFeasibleIndvAppear=0;
	static int sumOfGenerationsWhenAllSolutonsAreFeasible = 0;
	static int numberWhenFirstFeasibleIndvAppear=0;
	static int numberWhenAllSolutonsAreFeasible = 0;
	
	

	public static void traveresDirectory(File[] files) {
	    for (File file : files) {
	        if (file.isDirectory()) {
	            //System.out.println("Directory: " + file.getName());
	            traveresDirectory(file.listFiles()); // Calls same method again.
	        } else {
	        	if(file.getName().equals("trackingHardConstraints") &&
	        			file.length() != 0) {
	        		readWhenFirstFeasibleIndvAppear(file.getAbsolutePath());
	        		}
	        	else if(file.getName().equals("trackingSoftConstraints") &&
	        			file.length() != 0) {
	        		readWhenAllIndvAreFeasible(file.getAbsolutePath());
	        	}
	        	}
	    }
	}
	
	static void readWhenFirstFeasibleIndvAppear(String fileName){
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		    String line = br.readLine();
		    sumOfGenerationWhenFirstFeasibleIndvAppear += Integer.parseInt(line);
		    numberWhenFirstFeasibleIndvAppear += 1;
		    
		    br.close();
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	
	static void readWhenAllIndvAreFeasible(String fileName){
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		    String line = br.readLine();
		    String []parts = line.split(" ");
		    sumOfGenerationsWhenAllSolutonsAreFeasible += Integer.parseInt(parts[0]);
		    numberWhenAllSolutonsAreFeasible += 1;
		    
		    br.close();
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	public static void main(String[] args) {
			File[] files = new File(args[0]).listFiles();
			//writer = new PrintWriter(args[0]+"\\mergeFronts", "UTF-8");
			traveresDirectory(files);
			
			System.out.println(sumOfGenerationWhenFirstFeasibleIndvAppear/numberWhenFirstFeasibleIndvAppear);
			System.out.println(sumOfGenerationsWhenAllSolutonsAreFeasible/numberWhenAllSolutonsAreFeasible);
			
		
	}

}
