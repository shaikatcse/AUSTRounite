package aust.cse.routine.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingConstraints;
import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingObjectives;
import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingObjectivesV2;
import jmetal.encodings.variable.Permutation;



public class EvaluateIndvFromFile {

	final int numberOfObjectives=2;
	
	final int numberOfTheorySlots=378;
	final int numberOfLabSlots = 144;
	
	Connection dbConnection;
	File VariableFile;
	int [][]allVaribalearray;
	
	ArrayList<CourseInfo> courseInfo;
	ArrayList<SlotInfo> slotInfo;
	
	ModellingObjectivesV2 modellingObjectives;
	ModellingConstraints modellingConstraints;
	
		EvaluateIndvFromFile(String varFileName){
			
	
			//read courseInfo from courInfo.csv file
		    readCourseInfoFromFile();

		    //read slotInfo from slotInfo.csv file
		    readSlotInfoFromFile();
	
		    modellingObjectives = new ModellingObjectivesV2();
		    modellingConstraints = new ModellingConstraints();
		    
		    List<int[]> variableRow = readVariablesFromFile(varFileName);

		    for(int i=0;i<variableRow.size();i++) {
		    	
		    	//clear map
		    	modellingObjectives.clearAllMaps();
		    	modellingConstraints.clearAllMaps();
		    	fillClassroomTimeslotTable(variableRow.get(i));
			    
		    	String []objectives = evaluate(variableRow.get(i));
			    String constraints="0";
			
					constraints=evaluateConstraints(variableRow.get(i));
				
		    	
		    }
		    
		    
		}
		
		List<int[]> readVariablesFromFile(String varFile) {
			  BufferedReader br = null;
			    String line = "";
			    String splitBy = " ";
			    
			    List<int[]> variableRow = new ArrayList<int[]>();
			    
			    
				try {
				br = new BufferedReader(new FileReader(varFile));
				while ((line = br.readLine()) != null) {

		          // use comma as separator
		          String[] info = line.split(splitBy);
		          int [] row = new int[info.length];
		          for(int i=0;i<info.length;i++) {
		        	  row[i]=Integer.parseInt(info[i]);
		          }
		          variableRow.add(row);
		        
				}
				}catch(IOException e) {
					e.printStackTrace();
				}
				return variableRow;
			
		}
		
		void readCourseInfoFromFile() {
			  BufferedReader br = null;
			    String line = "";
			    String csvFile = "CourseInfo.csv";
			    String cvsSplitBy = ",";
			    
				courseInfo = new ArrayList<CourseInfo>();
				try {
				br = new BufferedReader(new FileReader(csvFile));
				line = br.readLine(); 
				while ((line = br.readLine()) != null) {

		          // use comma as separator
		          String[] info = line.split(cvsSplitBy);

		          CourseInfo infoClass = new CourseInfo(
		          		Integer.parseInt(info[0]), info[1], info[2], info[3], info[4], info[5] );

		          courseInfo.add(infoClass);
				}
				}catch(IOException e) {
					e.printStackTrace();
				}
		      }
		  
		  void readSlotInfoFromFile(){
			  BufferedReader br = null;
			    String line = "";
			    String csvFile = "SlotInfo.csv";
			    String cvsSplitBy = ",";

			    slotInfo = new ArrayList<SlotInfo>();
				try {
				    br = new BufferedReader(new FileReader(csvFile));
					line = br.readLine(); 
					while ((line = br.readLine()) != null) {
			
			            // use comma as separator
			            String[] info = line.split(cvsSplitBy);
			
			            SlotInfo infoSlot = new SlotInfo(
			            		info[0], info[1], info[2], info[3], info[4], info[5], info[6], info[7] );
			
			            slotInfo.add(infoSlot);
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
		    
		  
		  }

	
	public void fillClassroomTimeslotTable(int []variableArray) {
		  
		   
		  SlotInfo slotInfo;
		  CourseInfo courseInfo;
		    
		
		    int length=variableArray.length;
		    
		    for(int i=0; i<length; i++) {
		    	
		    	slotInfo = SlotInfo.searchSlotInfoArryList(this.slotInfo, i+"");
		    	courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo, variableArray[i]);
		    	
		    	modellingObjectives.fillUpTheMap(slotInfo, courseInfo);
		    	modellingConstraints.updateAllMaps(slotInfo, courseInfo);
		    	
		    	
		    
		    }	          
	  }
	
	
	public String[] evaluate(int []variableArray) {
	    
	    String []objectives = new String[2];
		
	    objectives[0] = round(modellingObjectives.calculateTotalStudentsTime(), 2);
	    
	    objectives[1]=round(modellingObjectives.calculateTotalTeachertime(),2);
	    
	    
	    
	    return objectives;
	    
 

	}	  
	
	
	  public String evaluateConstraints(int []variableArray) {
			double totalConstraints = 0;
			int numberOfConstraints = 0;

			/*double calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab = 
					modellingConstraints.calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab();
			if (calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab > 0.0) {
				totalConstraints += (-1 * calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab);
				numberOfConstraints++;
			}
			
			double calculateConstraintsForNotMoreThanTwoLabsInADayForStudents = 
					modellingConstraints.calculateConstraintsNotMoreThanTwoLabClassAndOneLab();
			if(calculateConstraintsForNotMoreThanTwoLabsInADayForStudents > 0) {
				totalConstraints += (-1*calculateConstraintsForNotMoreThanTwoLabsInADayForStudents);
				numberOfConstraints ++;
			}
			
			double calculateConstraintsNotMoreThanFourTheoryClassAndOneLab = 
					modellingConstraints.calculateConstraintsNotMoreThanFourTheoryClassAndOneLab();
			if(calculateConstraintsNotMoreThanFourTheoryClassAndOneLab > 0) {
				totalConstraints += (-1*calculateConstraintsNotMoreThanFourTheoryClassAndOneLab);
				numberOfConstraints ++;
			}*/
			
					
			double calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay = 
				modellingConstraints.calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay();
			if(calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay>0) {
				totalConstraints += (-1*calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay);
				numberOfConstraints ++;
			}
			
			double calculatingConstraintsOfSameTimeClassForATeacher = 
					modellingConstraints.calculatingConstraintsOfSameTimeClassForATeacher();
				if(calculatingConstraintsOfSameTimeClassForATeacher>0) {
					totalConstraints += (-1*calculatingConstraintsOfSameTimeClassForATeacher);
					numberOfConstraints ++;
				}
			
			
			return totalConstraints+"";
	  }

	  
	  public String round(double value, int places) {
		    if (places < 0) throw new IllegalArgumentException();

		    long factor = (long) Math.pow(10, places);
		    value = value * factor;
		    long tmp = Math.round(value);
		    return (double) tmp / factor+"";
		}

	  void copyFile(String fileName) {
			String strSourceFile="C:\\Users\\shaik\\Desktop\\TimeTableTestDB.sqlite";
			String strDestinationFile=fileName;
			
			try
			{
				//create FileInputStream object for source file
				FileInputStream fin = new FileInputStream(strSourceFile);
				
				//create FileOutputStream object for destination file
				FileOutputStream fout = new FileOutputStream(strDestinationFile);
				
				byte[] b = new byte[1024];
				int noOfBytes = 0;
				
	
				
				//read bytes from source file and write to destination file
				while( (noOfBytes = fin.read(b)) != -1 )
				{
					fout.write(b, 0, noOfBytes);
				}
				
				System.out.println("File copied!");
				
				//close the streams
				fin.close();
				fout.close();			
				
			}
			catch(FileNotFoundException fnf)
			{
				System.out.println("Specified file not found :" + fnf);
			}
			catch(IOException ioe)
			{
				System.out.println("Error while copying file :" + ioe);
			}
		  
	  }
	  
	  
	public static void main(String agrs[]) {
		//new DatabaseFromFile(".\\results\\VAR_Elitist_0"); 
		new EvaluateIndvFromFile(".\\results\\testV2\\run0\\VAR_NSGAII");
		
	
	}
	
}
