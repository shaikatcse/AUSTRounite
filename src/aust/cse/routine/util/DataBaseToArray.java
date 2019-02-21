package aust.cse.routine.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingConstraints;
import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingObjectives;
import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingObjectivesV2;
import jmetal.core.Solution;

public class DataBaseToArray {
	
	Connection dbConnection;

	ArrayList<CourseInfo> courseInfo;
	ArrayList<SlotInfo> slotInfo;

	ModellingObjectivesV2 modellingObjectives;
	ModellingConstraints modellingConstraints;
	public Connection connectDatabase() {
		try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\shaik\\Desktop\\Spring18OriginalRoutine1.sqlite");
            return conn;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
	
	public DataBaseToArray() {
		dbConnection = connectDatabase();
		
		modellingObjectives= new ModellingObjectivesV2();
		modellingConstraints = new ModellingConstraints();
		
		//read courseInfo from courInfo.csv file
	    readCourseInfoFromFile();

	    //read slotInfo from slotInfo.csv file
	    readSlotInfoFromFile();

		
	}
	
	SlotInfo getSlotInfo(String slotID) {
		int i=0;
		for(;i<slotInfo.size();i++) {
			if(slotInfo.get(i).getSlotID().equals(slotID))
				break;
		}
		if(i<slotInfo.size())
			return slotInfo.get(i);
		else return null;
	}
	
	CourseInfo getCourseInfo(String courseNo, String section, String studentGroup) {
		int i=0;
		for(;i<courseInfo.size();i++) {
			CourseInfo c;
			c= courseInfo.get(i);
			if(c.getCourseNo().equals(courseNo) 
					&& c.getAssignedSection().equals(section) &&c.getAssignedLabSection().equals(studentGroup) ) {
				break;
			}
		}
		if(i<courseInfo.size())
			return courseInfo.get(i);
		else return null;
	}
	
	void queryToExtractAllDataFromCourseClassRoomTimeSlotTableAndFilltheTimeSlot() {
		String query="SELECT * from CourseClassroomTimeslot";
		
		try{
			PreparedStatement statement = dbConnection.prepareStatement(query);
			
			ResultSet rs = statement.executeQuery();
			int i=1;
			while (rs.next()) {
				System.out.println(i);
				String courseNo=rs.getString("CourseNo");
				String section = rs.getString("Section");
				String studentGroup = rs.getString("StudentGroup");
				String slotId = rs.getString("slotID");
				
				CourseInfo c = getCourseInfo(courseNo, section, studentGroup);
				SlotInfo s = getSlotInfo(slotId);
				
				if(i==45 || i==21) {
					System.out.println("watch out");
				}
				modellingObjectives.fillUpTheMap(s, c);
				modellingConstraints.updateAllMaps(s, c);
				i++;
			
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

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
	  
	  void printInfo(){
		  modellingObjectives.PrintAllInfo();
		  System.out.println(modellingObjectives.calculateTotalTime());
	  }
	  
	  
	
	public static void main(String args[]) {
		DataBaseToArray dbToArray = new DataBaseToArray();
		dbToArray.queryToExtractAllDataFromCourseClassRoomTimeSlotTableAndFilltheTimeSlot();
		dbToArray.printInfo();
	}
	
}
