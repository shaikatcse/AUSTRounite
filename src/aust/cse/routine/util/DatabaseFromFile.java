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



public class DatabaseFromFile {

	final int numberOfObjectives=2;
	
	final int numberOfTheorySlots=378;
	final int numberOfLabSlots = 144;
	
	Connection dbConnection;
	File VariableFile;
	int [][]allVaribalearray;
	
	int numberOfDatabaseAccess=0;
	
	
	ArrayList<CourseInfo> courseInfo;
	ArrayList<SlotInfo> slotInfo;
	
	public Connection connectDatabase() {
		try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\shaik\\Desktop\\TimeTableTestDB.sqlite");
            return conn;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
	}
   
		DatabaseFromFile(String varFileName){
			//connect the database
			dbConnection=connectDatabase();
			
	
			//read courseInfo from courInfo.csv file
		    readCourseInfoFromFile();

		    //read slotInfo from slotInfo.csv file
		    readSlotInfoFromFile();
	
		    
		    
		    List<int[]> variableRow = readVariablesFromFile(varFileName);

		    for(int i=0;i<variableRow.size();i++) {
		    	deleteAllRowsFromCourseClassroomTimeslot();	
		    	fillClassroomTimeslotTable(variableRow.get(i));
			    	String []objectives = evaluate(variableRow.get(i));
			    	String constraints="0";
			
					constraints=evaluateConstraints(variableRow.get(i));
				if(numberOfObjectives==1) {
		    		copyFile(".\\result_database\\singleobjective\\run1\\database_o1_"+objectives[0]
			    			+"_con_"+constraints+".sqlite");
			    		
		    	}
			    	else if(numberOfObjectives==2) {
		    	copyFile(".\\result_database\\multiobjective\\run1\\database_o1_"+objectives[0]
		    			+"_o2_"+objectives[1]+"_con_"+constraints+".sqlite");}
				
				constraints=evaluateConstraints(variableRow.get(i));
				if(numberOfObjectives==1) {
		    		copyFile(".\\result_database\\singleobjective\\run1\\database_o1_"+objectives[0]
			    			+"_con_"+constraints+".sqlite");
			    		
		    	}
			    	else if(numberOfObjectives==2) {
		    	copyFile(".\\results\\demo\\run0\\database_o1_"+objectives[0]
		    			+"_o2_"+objectives[1]+"_con_"+constraints+".sqlite");}
		    	
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
		    
		    String bigInsertQuery;
		
		    int length=variableArray.length;
		    bigInsertQuery="INSERT INTO 'CourseClassroomTimeslot'";
		    //forint i=0; i<length; i++) {
		    	
		    
		    for(int i=0; i<500; i++) {
		    	
		    	slotInfo = SlotInfo.searchSlotInfoArryList(this.slotInfo, i+"");
		    	courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo, (variableArray[i]));
		    	
		    	if(i==0) {
		    		bigInsertQuery=bigInsertQuery +" SELECT '" +courseInfo.getCourseNo() +"' AS 'courseNo', '"+slotInfo.getSlotID()+"' AS 'slotId', '"+slotInfo.getRoomNo()+"' AS 'RoomNo', '"+ courseInfo.getAssignedSection() +"' AS 'Section' ,'"+courseInfo.getAssignedLabSection()+ "' AS 'StudentGroup' ,'"+slotInfo.getSession()+"' AS 'Session'";		
		    	//	System.out.println(bigInsertQuery);
		    	}else {
		    		bigInsertQuery=bigInsertQuery +" UNION ALL SELECT '" +courseInfo.getCourseNo() +"', '"+slotInfo.getSlotID()+"', '"+slotInfo.getRoomNo()+"', '"+ courseInfo.getAssignedSection() +"' ,'"+courseInfo.getAssignedLabSection()+"' ,'"+slotInfo.getSession()+ "'";
		    	}
		    	
		    	
		    
		    }
			/*long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			long actualMemUsed=afterUsedMem-beforeUsedMem;
			System.out.println("String: " + actualMemUsed);
		*/
			
	    	
		    //System.out.println(bigInsertQuery);
		    try {
				PreparedStatement statement = dbConnection.prepareStatement(bigInsertQuery);
				statement.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
			
		    bigInsertQuery =null;
		    bigInsertQuery = "INSERT INTO 'CourseClassroomTimeslot'";
		    
		 for(int i=500; i<length; i++) {
		    	
		    	slotInfo = SlotInfo.searchSlotInfoArryList(this.slotInfo, i+"");
		    	courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo, variableArray[i]);
		    	
		    	//fill the table
		    	//insertARowToCourseClassroomTimeslotTable(courseInfo, slotInfo);
		    	if(i==500) {
		    		bigInsertQuery=bigInsertQuery + " SELECT '" +courseInfo.getCourseNo() +"' AS 'courseNo', '"+slotInfo.getSlotID()+"' AS 'slotId', '"+slotInfo.getRoomNo()+"' AS 'RoomNo', '"+ courseInfo.getAssignedSection() +"' AS 'Section' ,'"+courseInfo.getAssignedLabSection()+ "' AS 'StudentGroup' ,'"+slotInfo.getSession()+"' AS 'Session'";		
		    	}else {
		    		bigInsertQuery=bigInsertQuery + " UNION ALL SELECT '" +courseInfo.getCourseNo() +"', '"+slotInfo.getSlotID()+"', '"+slotInfo.getRoomNo()+"', '"+ courseInfo.getAssignedSection() +"' ,'"+courseInfo.getAssignedLabSection()+"' ,'"+slotInfo.getSession()+ "'";;
		    	}
		    	
		    	
		    }
		    //System.out.println(bigInsertQuery);
		    try {
				PreparedStatement statement = dbConnection.prepareStatement(bigInsertQuery);
				bigInsertQuery=null;
				
				statement.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		          
	  }
	
public double calculateObjective1() {
		
		numberOfDatabaseAccess++;
		if(numberOfDatabaseAccess%2000==0) {
			try {
				dbConnection.close();
				dbConnection=connectDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		double obj1 = 0.0;
	
		
		String query="SELECT DISTINCT Course.ysID, Timeslot.Day, MIN(time(Timeslot.startTime)) as Starting_Time, MAX(time(Timeslot.endTime)) as Finishing_Time, CourseClassroomTimeslot.Section, CourseClassroomTimeslot.StudentGroup, ((MAX(strftime('%s', Timeslot.endTime)) - MIN(strftime('%s', Timeslot.startTime)))/3600.0) as duration, count(Timeslot.Day) from Timeslot, CourseClassroomTimeslot, Course where Timeslot.slotID = CourseClassroomTimeslot.slotID and CourseClassroomTimeslot.CourseNo = Course.CourseNo "+
    			"GROUP BY Timeslot.DAY,CourseClassroomTimeslot.Section,Course.ysID, CourseClassroomTimeslot.StudentGroup "+
    			"ORDER BY Course.ysID ASC, CourseClassroomTimeslot.section ASC, case when Day= 'Saturday' then 1 when Day= 'Sunday' then 2 when Day= 'Monday' then 3 "+
    			              "when Day= 'Tuesday' then 4 when Day= 'Wednesday' then 5 "+ 
    			              "else 6 end asc, time(startTime) ASC";
		
		try{
			PreparedStatement statement = dbConnection.prepareStatement(query);
    	
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				//System.out.println("duration" + rs.getString("duration") );
				obj1 += Double.parseDouble(rs.getString("duration"));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	return obj1;
	}
	
	public double calculateObjective2() {
		
		
		numberOfDatabaseAccess++;
			if(numberOfDatabaseAccess%2000==0) {
			try {
				dbConnection.close();
				dbConnection=connectDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		double obj2 = 0.0;
		
		
		//teachers' query updated by 16/7/2018

		String query = "select T.TeacherName, TAC.CourseNo, CCT.Section, CCT.StudentGroup, C.ysID, CCT.slotID, MIN(time(TS.startTime)) as Starting_Time, MAX(time(TS.endTime)) as Finishing_Time, TS.Day, "
		             + "((MAX(strftime('%s', TS.endTime)) - MIN(strftime('%s', TS.startTime)))/3600.0) as duration "
		             + "from Teacher T, TeacherAssignedCourses TAC, CourseClassroomTimeslot CCT, Timeslot TS, Course C "
		             + "where T.TeacherID = TAC.TeacherID and TAC.CourseNo = CCT.CourseNo and CCT.slotID = TS.slotID and CCT.CourseNo = C.CourseNo and CCT.Section = TAC.Section and Tac.StudentGroup = CCT.StudentGroup "
		             + "GROUP BY T.TeacherName, TS.Day "
		             + "ORDER BY T.TeacherName, case when Day= 'Saturday' then 1 "
		             + "when Day= 'Sunday' then 2 "
		             + "when Day= 'Monday' then 3 "
		             + "when Day= 'Tuesday' then 4 "
		             + "when Day= 'Wednesday' then 5 " 
		             + "else 6 "
		             + "end asc, time(startTime)"; 
		try{
			PreparedStatement statement = dbConnection.prepareStatement(query);
    	
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				//System.out.println("duration" + rs.getString("duration") );
				obj2 += Double.parseDouble(rs.getString("duration"));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	return obj2;
	}
	
/*	public double calculateObjective2() {
		double obj2 = 0.0;
		String query="select T.TeacherName, TAC.CourseNo, CCT.Section, CCT.StudentGroup, C.ysID, CCT.slotID, MIN(time(TS.startTime)) as Starting_Time, MAX(time(TS.endTime)) as Finishing_Time, TS.Day,"
    			+" ((MAX(strftime('%s', TS.endTime)) - MIN(strftime('%s', TS.startTime)))/3600.0) as duration"
    			+" from Teacher T, TeacherAssignedCourses TAC, CourseClassroomTimeslot CCT, Timeslot TS, Course C"
    			+" where T.TeacherID = TAC.TeacherID and TAC.CourseNo = CCT.CourseNo and CCT.slotID = TS.slotID and CCT.CourseNo = C.CourseNo and CCT.Section = TAC.Section and Tac.StudentGroup = CCT.StudentGroup"
    			+" GROUP BY T.TeacherName, TS.Day "
    			+" ORDER BY T.TeacherName, case when Day= 'Saturday' then 1 "
    			              +" when Day= 'Sunday' then 2"
    			              +" when Day= 'Monday' then 3"
    			              +" when Day= 'Tuesday' then 4"
    			              +" when Day= 'Wednesday' then 5 else 6"
    			              +" end asc, time(startTime)"; 
		
		try{
			PreparedStatement statement = dbConnection.prepareStatement(query);
    	
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				//System.out.println("duration" + rs.getString("duration") );
				obj2 += Double.parseDouble(rs.getString("duration"));
			}	
		}catch (Exception e) {
			e.printStackTrace();
		}
	return obj2;
	}*/
	
	public int calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab() {
		numberOfDatabaseAccess++;
		if(numberOfDatabaseAccess%2000==0) {
			try {
				dbConnection.close();
				dbConnection=connectDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		int numberOfRow = 0;

		// Not more than three theory class and a lab in a day for A1/B1/C1

		String query1 = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Quantity "
				+ "from TimeSlot T, CourseClassroomTimeslot CCT, Course C "
				+ "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and CCT.StudentGroup<>2 "
				+ "GROUP BY T.DAY,CCT.Section,ysID" + " having count(CCT.CourseNo) >4 "
				+ "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1" + " when Day= 'Sunday' then 2 "
				+ "when Day= 'Monday' then 3" + " when Day= 'Tuesday' then 4" + " when Day= 'Wednesday' then 5 "
				+ "else 6 end asc, time(startTime) ASC";

		try {
			PreparedStatement statement = dbConnection.prepareStatement(query1);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// System.out.println("duration" + rs.getString("duration") );
				numberOfRow++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Not more than three theory class and a lab in a day for A2/B2/C2
		String query2 = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Quantity "
				+ "from TimeSlot T, CourseClassroomTimeslot CCT, Course C "
				+ "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and CCT.StudentGroup<>1 "
				+ "GROUP BY T.DAY,CCT.Section,ysID " + "having count(CCT.CourseNo) >4 "
				+ "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1 " + "when Day= 'Sunday' then 2 "
				+ "when Day= 'Monday' then 3 " + "when Day= 'Tuesday' then 4 " + "when Day= 'Wednesday' then 5 "
				+ "else 6 end asc, time(startTime) ASC";

		try {
			PreparedStatement statement = dbConnection.prepareStatement(query2);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// System.out.println("duration" + rs.getString("duration") );
				numberOfRow++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return numberOfRow;
	}

	public int calculateConstraintsForNotMoreThanTwoLabsInADayForStudents() {
		numberOfDatabaseAccess++;
		if(numberOfDatabaseAccess%2000==0) {
			try {
				dbConnection.close();
				dbConnection=connectDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		int numberOfRow = 0;
		// Not more than two labs in a day for students of A1/B1/C1
		String query1 = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Lab_Quantity "
				+ "from TimeSlot T, CourseClassroomTimeslot CCT, Course C "
				+ "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and C.CourseType = 'Lab' and CCT.StudentGroup<>2 "
				+ "GROUP BY T.DAY,CCT.Section,ysID " + "having count(C.CourseNo)>2 "
				+ "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1 " + "when Day= 'Sunday' then 2 "
				+ "when Day= 'Monday' then 3 " + "when Day= 'Tuesday' then 4 " + "when Day= 'Wednesday' then 5 "
				+ "else 6 end asc, time(startTime) ASC";
		try {
			PreparedStatement statement = dbConnection.prepareStatement(query1);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// System.out.println("duration" + rs.getString("duration") );
				numberOfRow++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Not more than two labs in a day for students of A2/B2/C2
		String query2 = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Lab_Quantity "
				+ "from TimeSlot T, CourseClassroomTimeslot CCT, Course C "
				+ "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and C.CourseType = 'Lab' and CCT.StudentGroup<>1 "
				+ "GROUP BY T.DAY,CCT.Section,ysID " + "having count(C.CourseNo)>2 "
				+ "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1 " + "when Day= 'Sunday' then 2 "
				+ "when Day= 'Monday' then 3 " + "when Day= 'Tuesday' then 4 " + "when Day= 'Wednesday' then 5 "
				+ "else 6 end asc, time(startTime) ASC";
		try {
			PreparedStatement statement = dbConnection.prepareStatement(query2);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// System.out.println("duration" + rs.getString("duration") );
				numberOfRow++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return numberOfRow;
	}
	
	public int calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInADay(){
		numberOfDatabaseAccess++;
		if(numberOfDatabaseAccess%2000==0) {
			try {
				dbConnection.close();
				dbConnection=connectDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		int numberOfRow=0;
		
		String query = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count( Distinct Cl.RoomNo) as Room_Quantity, CCT.RoomNo "
				     + "from TimeSlot T, CourseClassroomTimeslot CCT, Course C, Classroom Cl "
				     + "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and CCT.RoomNo = Cl.RoomNo and C.CourseType = 'Theory' " 
				     + "GROUP BY T.DAY,CCT.Section,ysID "
				     + "having count(Distinct Cl.RoomNo)>=2 "
				     + "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1 "
				     + "when Day= 'Sunday' then 2 "
				     + "when Day= 'Monday' then 3 "
				     + "when Day= 'Tuesday' then 4 "
				     + "when Day= 'Wednesday' then 5 " 
				     + "else 6 "
				     + "end asc, time(startTime) ASC";
		
		try {
			PreparedStatement statement = dbConnection.prepareStatement(query);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// System.out.println("duration" + rs.getString("duration") );
				numberOfRow++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return numberOfRow;

	}
	

	public int calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay(){
		numberOfDatabaseAccess++;
		if(numberOfDatabaseAccess%2000==0) {
			try {
				dbConnection.close();
				dbConnection=connectDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		int numberOfRow=0;
		
		String query = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count( Distinct Cl.RoomNo) as Room_Quantity, CCT.RoomNo "
				     + "from TimeSlot T, CourseClassroomTimeslot CCT, Course C, Classroom Cl "
				     + "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and CCT.RoomNo = Cl.RoomNo " 
				     + "GROUP BY T.DAY,CCT.Section,ysID, CCT.Session "
				     + "having count(Distinct Cl.RoomNo)>=2 "
				     + "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1 "
				     + "when Day= 'Sunday' then 2 "
				     + "when Day= 'Monday' then 3 "
				     + "when Day= 'Tuesday' then 4 "
				     + "when Day= 'Wednesday' then 5 " 
				     + "else 6 "
				     + "end asc, time(startTime) ASC";
		
		try {
			PreparedStatement statement = dbConnection.prepareStatement(query);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// System.out.println("duration" + rs.getString("duration") );
				numberOfRow++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return numberOfRow;

	}

	
	public int calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay() {
		numberOfDatabaseAccess++;
		if(numberOfDatabaseAccess%2000==0) {
			try {
				dbConnection.close();
				dbConnection=connectDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		int numberOfRow=0;
		
		String query = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Theory_Quantity "
				     + "from TimeSlot T, CourseClassroomTimeslot CCT, Course C "
				     + "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and C.CourseType = 'Theory' " 
				     + "GROUP BY T.DAY,CCT.Section,ysID "
				     + "having count(C.CourseNo) >4 or count(C.CourseNo) <2 "
				     + "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1 "
				     + "when Day= 'Sunday' then 2 "
				     + "when Day= 'Monday' then 3 "
				     + "when Day= 'Tuesday' then 4 "
				     + "when Day= 'Wednesday' then 5 " 
				     + "else 6 "
				     + "end asc, time(startTime) ASC";
		try {
			PreparedStatement statement = dbConnection.prepareStatement(query);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// System.out.println("duration" + rs.getString("duration") );
				numberOfRow++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return numberOfRow;
		
	}

	
	public void deleteAllRowsFromCourseClassroomTimeslot(){
		//delete the table first
	    try {
	        
	    	PreparedStatement statement = dbConnection.prepareStatement(
			             "delete from CourseClassroomTimeslot");
			statement.executeUpdate();
		  
	    
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String[] evaluate(int []variableArray) {
	    
	    String []objectives;
		if(numberOfObjectives==1) {
			double obj1=calculateObjective1();
			obj1=Math.round(obj1 * 100.0) / 100.0;
			objectives=new String[] {obj1+""};
	    
			
		}else if(numberOfObjectives==2) {
			double obj1=calculateObjective1();
			obj1=Math.round(obj1 * 100.0) / 100.0;
			
		    double obj2=calculateObjective2();
		    obj2=Math.round(obj2 * 100.0) / 100.0;
		    objectives=new String[] {obj1+"", obj2+""};
		}
	    return objectives;
	    
 

	}	  
	
	
	  public String evaluateConstraints(int []variableArray) {
			double totalConstraints = 0;
			int numberOfConstraints = 0;

			
			int calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab = calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab();
			if (calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab > 0) {
				totalConstraints += (-1 * calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab);
				numberOfConstraints++;
			}
			
			int calculateConstraintsForNotMoreThanTwoLabsInADayForStudents = calculateConstraintsForNotMoreThanTwoLabsInADayForStudents();
			if(calculateConstraintsForNotMoreThanTwoLabsInADayForStudents > 0) {
				totalConstraints += (-1*calculateConstraintsForNotMoreThanTwoLabsInADayForStudents);
				numberOfConstraints ++;
			}
			
			int calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay = 
				calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay();
			if(calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay>0) {
				totalConstraints += (-1*calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay);
				numberOfConstraints ++;
			}
	/*
			int calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay
					= calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay();
			if(calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay>0) {
				totalConstraints += (-1*calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay);
				numberOfConstraints++;
			}*/
			
			
			return totalConstraints+"";
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
		new DatabaseFromFile(".\\results\\demo\\run0\\FIS_VAR_NSGAII");
	
	}
	
}
