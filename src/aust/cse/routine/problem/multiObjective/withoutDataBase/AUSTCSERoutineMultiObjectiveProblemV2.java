//  TSP.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package aust.cse.routine.problem.multiObjective.withoutDataBase;


import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.Permutation;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.io.*;
import java.nio.channels.FileLockInterruptionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;

/**
 * Class representing a TSP (Traveling Salesman Problem) problem.
 */
public class AUSTCSERoutineMultiObjectiveProblemV2 extends Problem {

	
	final int numberOfTheorySlots=378;
	final int numberOfLabSlots = 144;
	final int numberofTheoryCourses = 300;
	
	final int totalnumberOfTheorySession = 126;
	final int numberOfTheorySession = 100;
	
	
	Connection dbConnection;
	
	ArrayList<CourseInfo> courseInfo;
	ArrayList<SlotInfo> slotInfo;
	
	ModellingObjectivesV2 modellingObjectives;
	ModellingConstraints modellingConstraints;
	
	int numberOfDatabaseAccess=0;
	
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
	
	boolean searchATheoryCourseInVector(int []vector, int courseId) {
		//search the vector for the course
		int i=0;
		for(;i<numberOfTheorySlots;i++) {
			if(vector[i]==courseId) {
				break;
			}
		}
		if(i>=numberOfTheorySlots)
			return false;
		else return true;
	}
	
	boolean searchALabCourseInVector(int []vector, int courseId) {
		//search the vector for the course
		int i=numberOfTheorySlots;
		for(;i<numberOfTheorySlots+numberOfLabSlots;i++) {
			if(vector[i]==courseId) {
				break;
			}
		}
		if(i>=(numberOfTheorySlots+numberOfLabSlots))
			return false;
		else return true;
	}
	
	void numberOfAvailablecourses(int [] vector, int courseId){
		int randomCourseId;
		//search a course until found 
		CourseInfo aCourse, randomCourse;

		//for tracking
		int counter=0;
		aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
		for(int i=0;i<numberOfTheorySlots;i++) {
			randomCourse = CourseInfo.srachCourseInfoArryList(courseInfo, i);
			if(aCourse.getCourseYearSemester().equals(randomCourse.getCourseYearSemester())
			&& aCourse.getAssignedSection().equals(randomCourse.getAssignedSection())
			&& !searchATheoryCourseInVector(vector, i)) {
				counter++;
			}
		}
		System.out.println("total available course: "+counter);
	}
	
	int findACourseWithSameYearSemerterAndSectionAndNotInVector(int [] vector, int courseId){
		int randomCourseId;
		//search a course until found 
		CourseInfo aCourse, randomCourse;
		
		
		Random rand = new Random();
		
		aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);

		do {
			//randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberofTheoryCourses);
			randomCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);
		}while(!aCourse.getCourseYearSemester().equals(randomCourse.getCourseYearSemester())
				|| !aCourse.getAssignedSection().equals(randomCourse.getAssignedSection())
				|| searchATheoryCourseInVector(vector, randomCourseId));
		
		
		
		return randomCourseId;
	}

	int findATheoryCourseWhichIsNotAppearInVector(int []vector) {
		int randomCourseId;
		CourseInfo aCourse;
		//search a course until found 
		Random rand = new Random();
		
		do {
			//randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberofTheoryCourses);
			aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);

		}while(searchATheoryCourseInVector(vector, randomCourseId) || aCourse.getCourseNo().equals("freetime"));
		
		return randomCourseId;
		
	}
	
	int findALabCourseWhichIsNotAppearInVector(int []vector) {
		int randomCourseId;
		CourseInfo aCourse;
		//search a course until found 
		Random rand = new Random();
		
		do {
			//randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberOfLabSlots)+numberOfTheorySlots;
			aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);

		}while(searchALabCourseInVector(vector, randomCourseId)|| aCourse.getCourseNo().equals("freetime"));
		
		return randomCourseId;
		
	}
	
	boolean areStdentFreeInATheoryTimeSlot(int [] vector, int slotHead, int dayInNumber, CourseInfo courseInSlotHead) {

		//this is for tracking any theory course is in the same time slot that conflicts with the lab that we want to assigned 
		boolean areStudentsFree=false;
		SlotInfo slotHeadInfo = SlotInfo.searchSlotInfoArryList(slotInfo, slotHead+"");
		for(int i=dayInNumber*63;i<dayInNumber*63+63 && !areStudentsFree;i=i+3) {
				int slotHeadInLoop = i;
				SlotInfo slotHeadInLoopInfo = SlotInfo.searchSlotInfoArryList(slotInfo, slotHeadInLoop+"");
				if(vector[slotHeadInLoop]!=-1) {
					if(slotHeadInfo.getDay().equals(slotHeadInLoopInfo.getDay()) && slotHeadInfo.getStartTime().equals(slotHeadInLoopInfo.getStartTime())){
							CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[slotHeadInLoop]);
							if(courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
									&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
								areStudentsFree=true;
							}
						
					}
				}
				
			}
		
		return !areStudentsFree;
	}

	boolean areStdentFreeInALabTimeSlot(int [] vector, int slotHead, int dayInNumber, CourseInfo courseInSlotHead) {
		
		boolean areStudentFreeInTheorySlot=false;
		SlotInfo slotHeadInfo = SlotInfo.searchSlotInfoArryList(slotInfo, slotHead+"");
		for(int i=dayInNumber*63;i<dayInNumber*63+63 && !areStudentFreeInTheorySlot;i=i+3) {
				int slotHeadInLoop = i;
				SlotInfo slotHeadInLoopInfo = SlotInfo.searchSlotInfoArryList(slotInfo, slotHeadInLoop+"");
				if(vector[slotHeadInLoop]!=-1) {
					if(slotHeadInfo.getDay().equals(slotHeadInLoopInfo.getDay()) && slotHeadInfo.getStartTime().equals(slotHeadInLoopInfo.getStartTime())){
							CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[slotHeadInLoop]);
							if(courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
									&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
								areStudentFreeInTheorySlot=true;
							}
						
					}
				}
				
			}
		

		//this is for tracking any lab course is in the same time slot that conflicts with the lab that we want to assigned 
		boolean areStudentFreeLabSlot=false;
		for(int i=numberOfTheorySlots+dayInNumber*24;i<numberOfTheorySlots+dayInNumber*24+24;i++) {
			SlotInfo slotHeadInLoopInfo = SlotInfo.searchSlotInfoArryList(slotInfo, i+"");
			if(vector[i]!=-1) {
				if(slotHeadInfo.getDay().equals(slotHeadInLoopInfo.getDay()) && slotHeadInfo.getStartTime().equals(slotHeadInLoopInfo.getStartTime())){
					CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
					if(courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
							&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
						areStudentFreeLabSlot=true;
					}
					
				}
			}
		}
		
		if(areStudentFreeInTheorySlot || areStudentFreeLabSlot)
			return false;
		else
			return true;
	}
	
	boolean isThereThereMoreThan1TheorySessionInTheSameDay(int [] vector, int slotHead, int dayInNumber, CourseInfo courseInSlotHead){
		boolean areStudentshaveMoreThan1TheorySessionInTheSameDay=false;
		for(int i=dayInNumber*63;i<dayInNumber*63+63;i=i+3) {
			CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
			if(vector[i]!=-1) {
				if(courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
						&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
					areStudentshaveMoreThan1TheorySessionInTheSameDay=true;
				}
			}
		}
		return !areStudentshaveMoreThan1TheorySessionInTheSameDay;
	}
	
	boolean isThereMoreThan2LabsInTheSameDay(int [] vector, int slotHead, int dayInNumber, CourseInfo courseInSlotHead) {
		int counter=0;
		for(int i=numberOfTheorySlots+dayInNumber*24;i<numberOfTheorySlots+dayInNumber*24+24;i++) {
			CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
			if(vector[i]!=-1) {
				if(courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
						&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
					counter++;
				}
			}
		}
		if(counter>=2)
			return false;
		else 
			return true;
	}
	
	boolean isThereMoreThan3TheoryAnd1LabInTheSameDay(int [] vector, int slotHead, int dayInNumber, CourseInfo courseInSlotHead) {
		int counter=0;
		for(int i=dayInNumber*63;i<dayInNumber*63+63;i=i+3) {
			CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
			if(vector[i]!=-1) {
				if(courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
						&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
					counter+=3;
				}
			}
			
		}
		
		for(int i=numberOfTheorySlots+dayInNumber*24;i<numberOfTheorySlots+dayInNumber*24+24;i++) {
			CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
			if(vector[i]!=-1) {
				if(courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
						&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
					counter++;
				}
			}
		}
		if(counter>=4)
			return false;
		else 
			return true;
	}
	
	
	boolean isTheFreeSlotAssigned(int []vector, int freeSlotId) {
		int i=0;
		for(; i<vector.length;i++) {
			if(vector[i]==freeSlotId)
				break;
		}
		if(i>=vector.length)
			return false;
		else return true;
	}
	public Solution createVariable() {
	
		Random rand = new Random();
		
		 int [] vector_=new int[numberOfTheorySlots+numberOfLabSlots];
		 for(int i=0;i<vector_.length;i++)
			 vector_[i]=-1;
		 
		 for(int i=0;i<100;i++) {
			 //find a random slot which is not occupied 
			 int randomDay; // 0 to 5
			 int randomSlot; // 0 to 21 (in a day)
			 int slotHead;
			 
			//find a course that is not appeared in the vector
			 int courseId=findATheoryCourseWhichIsNotAppearInVector(vector_);
			 CourseInfo aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
			 
			 do {
				 randomSlot = rand.nextInt( 21);
				 randomDay = rand.nextInt(6);
				 slotHead= randomSlot*3+randomDay*63;
			 
			 }while(vector_[slotHead]!=-1 
					 || !areStdentFreeInATheoryTimeSlot(vector_, slotHead, randomDay, aCourse) 
					 || !isThereThereMoreThan1TheorySessionInTheSameDay(vector_, slotHead, randomDay, aCourse) 
					 || !isThereMoreThan2LabsInTheSameDay(vector_, slotHead, randomDay, aCourse) 
					 || !isThereMoreThan3TheoryAnd1LabInTheSameDay(vector_, slotHead, randomDay, aCourse) );
			
			 vector_[slotHead+0]=courseId;
			 vector_[slotHead+1]=findACourseWithSameYearSemerterAndSectionAndNotInVector(vector_, courseId);
			 vector_[slotHead+2]=findACourseWithSameYearSemerterAndSectionAndNotInVector(vector_, courseId);
			 	 	 
			
			 
		 }
		 
		/* int counter1=0;
		 for(int i=0;i<vector_.length;i++) {
			counter1=0;
			 for(int j=0;j<vector_.length;j++)
			 if( i == vector_[j]) {
				 counter1++;
			 }
			 System.out.println("i: "+i+" "+ counter1);
			 
		 }
		 
		 System.out.println();
		 
		 int counter2=0;
		 for(int i=0;i<vector_.length;i++) {
			 if(vector_[i]!=-1)
				 counter2++;
		 }
		 System.out.println(counter2);*/
		 
		 //assign theory freeslots randomly
		 for(int i=0;i<numberOfTheorySlots;i++) {
			
			// System.out.println(i);
			 if(vector_[i]==-1) {
				 int randomFreeSlot;
				 do{
					 randomFreeSlot = rand.nextInt((numberOfTheorySlots - numberofTheoryCourses)) + numberofTheoryCourses;
				 }while(isTheFreeSlotAssigned(vector_, randomFreeSlot));
				 
				 vector_[i]= randomFreeSlot;
			 }
		 }
		 
		  //assign labs randomly while not violating constraint 
		int randomLabSlot;
		 for(int i=0;i<97;i++) {
			
			 int randomDay; // 0 to 5
			 int randomSlot; // 0 to 21 (in a day)
			 int slotHead;
			
			 
			 int courseId=findALabCourseWhichIsNotAppearInVector(vector_);
			 CourseInfo aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
			 
			 
			 do {
				 
			
				 randomSlot = rand.nextInt(24);
				 randomDay = rand.nextInt(6);
				 slotHead= randomSlot+randomDay*24+numberOfTheorySlots;
			
			}while(vector_[slotHead]!=-1 
					 || !areStdentFreeInALabTimeSlot(vector_, slotHead, randomDay, aCourse)
					 || !isThereMoreThan2LabsInTheSameDay(vector_, slotHead, randomDay, aCourse) 
					 || !isThereMoreThan3TheoryAnd1LabInTheSameDay(vector_, slotHead, randomDay, aCourse) );
			 
			 vector_[slotHead]=courseId;
			 
		 }
		 
		 //assign lab freeslots randomly (the lab free slots starts from index 475
		 for(int i=numberOfTheorySlots;i<numberOfTheorySlots+numberOfLabSlots;i++) {
			 if(vector_[i]==-1) {
				 int randomFreeSlot;
				 
				 do {
					 randomFreeSlot = rand.nextInt(numberOfTheorySlots+numberOfLabSlots - 475) + 475 ; 
				 } while(isTheFreeSlotAssigned(vector_, randomFreeSlot));
					 
					 vector_[i]= randomFreeSlot;
				 
				 }
		 }
		 
	/*	 int counter;
		 for(int i=0;i<vector_.length;i++) {
			counter=0;
			 for(int j=0;j<vector_.length;j++)
				 if(i==vector_[j])
					 counter++;
			 System.out.println(i+ " " +counter);
		 }*/
		
		 Solution s=null;
			try {
				
				s = new Solution(this);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		((Permutation)s.getDecisionVariables()[0]).vector_=vector_;
		
		return s;
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





  /**
   * Creates a new TSP problem instance. It accepts data files from TSPLIB
   * @param filename The file containing the definition of the problem
   */
  public AUSTCSERoutineMultiObjectiveProblemV2(String solutionType) {
    
	  
	numberOfVariables_  = 1;
    numberOfObjectives_ = 2;
    numberOfConstraints_= 3;
    problemName_        = "AUSTCSERoutineProblem";
    solutionType_ = new PermutationSolutionType(this) ;

    length_       = new int[numberOfVariables_];
    
    //connect the database
    dbConnection=connectDatabase();
    
        
   // modelling objective
   modellingObjectives = new ModellingObjectivesV2();
   
   //modeliing constraints
   modellingConstraints = new ModellingConstraints();
   
    //read courseInfo from courInfo.csv file
    readCourseInfoFromFile();

    //read slotInfo from slotInfo.csv file
    readSlotInfoFromFile();
    try {
      if (solutionType.compareTo("Permutation") == 0)
        solutionType_ = new PermutationSolutionType(this) ;
      else {
        throw new JMException("Solution type invalid") ;
      }
    } catch (JMException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    length_      [0] = 522 ;
  } // TSP

  
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
  
  public void insertARowToCourseClassroomTimeslotTable(CourseInfo courseInfo, SlotInfo slotInfo) {
		String query = "insert into CourseClassroomTimeslot values ("+
						"\""+courseInfo.getCourseNo()+"\","+
						"\""+slotInfo.getSlotID()+"\","+
						"\""+slotInfo.getRoomNo()+"\","+
						"\""+courseInfo.getAssignedSection()+"\","+
						"\""+courseInfo.getAssignedLabSection()+"\")";
		try {
			PreparedStatement statement = dbConnection.prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
  
  public void fillClassroomTimeslotTable(Solution solution) {
	  
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
	  
	  SlotInfo slotInfo;
	    CourseInfo courseInfo;
	    
	    String bigInsertQuery;
	
	    int length=((Permutation)solution.getDecisionVariables()[0]).vector_.length;
	    bigInsertQuery="INSERT INTO 'CourseClassroomTimeslot'";
	    //forint i=0; i<length; i++) {
	    	
	    
	    for(int i=0; i<500; i++) {
	    	
	    	slotInfo = SlotInfo.searchSlotInfoArryList(this.slotInfo, i+"");
	    	courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo, ((Permutation)solution.getDecisionVariables()[0]).vector_[i]);
	    	
	    	modellingObjectives.fillUpTheMap(slotInfo, courseInfo);
	    	modellingConstraints.updateAllMaps(slotInfo, courseInfo);
	    	
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
	    	courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo, ((Permutation)solution.getDecisionVariables()[0]).vector_[i]);
	   
	     	modellingObjectives.fillUpTheMap(slotInfo, courseInfo);
	 	   
	    	
	    	//fill the table
	    	//insertARowToCourseClassroomTimeslotTable(courseInfo, slotInfo);
	    	
	    	if(i==500) {
	    		bigInsertQuery=bigInsertQuery + " SELECT '" +courseInfo.getCourseNo() +"' AS 'courseNo', '"+slotInfo.getSlotID()+"' AS 'slotId', '"+slotInfo.getRoomNo()+"' AS 'RoomNo', '"+ courseInfo.getAssignedSection() +"' AS 'Section' ,'"+courseInfo.getAssignedLabSection()+ "' AS 'StudentGroup' ,'"+slotInfo.getSession()+"' AS 'Session'";;		
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
  
  /**
   * Evaluates a solution
   * @param solution The solution to evaluate
   */
  public void evaluate(Solution solution) {
    
	  
	  
	  double fitness   ;

    
    //delete the table first
    try {
    	//long initTime = System.currentTimeMillis();
        
    	PreparedStatement statement = dbConnection.prepareStatement(
		             "delete from CourseClassroomTimeslot");
		statement.executeUpdate();
		//long estimatedTime = System.currentTimeMillis() - initTime;
	    //System.out.println("Time (evaluate:deletion): "+estimatedTime);
	  
    
    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    
    
   
    
    fillClassroomTimeslotTable(solution);
    
    
    double obj1=calculateObjective1();
    double ttt = modellingObjectives.calculateTotalTime();
    
    double obj2=calculateObjective2();

   
    solution.setObjective(0, obj1);
    solution.setObjective(1, -1*obj2);
  
  } // evaluate

  
  public void evaluateConstraints(Solution solution) throws JMException {
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

	/*	int calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay
				= calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay();
		if(calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay>0) {
			totalConstraints += (-1*calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay);
			numberOfConstraints++;
		}*/
		
		solution.setOverallConstraintViolation(totalConstraints);
		solution.setNumberOfViolatedConstraint(numberOfConstraints);
	}


 boolean isItATheoryCourse(int courseId) {
	 CourseInfo aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
	 if(aCourse.getCourseType().equals("Theory"))
		 return true;
	 else
		 return false;
	 
 }
  
 boolean isItALabCourse(int courseId) {
	 CourseInfo aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
	 if(aCourse.getCourseType().equals("Lab"))
		 return true;
	 else
		 return false;
	 
 }
 
 boolean isItAFreeCourseTime(int courseId) {
	 CourseInfo aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
	 if(aCourse.getCourseNo().equals("freetime"))
		 return true;	
	 else
		 return false;
	 
 }
 
 //it returns a slot number where a theory course is found in the Labslot
 //returning -1 means no such theory couse is found in the labslot
 int findATheoryCourseInLabSolt(int  []a) {
	 for(int i=numberOfTheorySlots;i<numberOfTheorySlots+numberOfLabSlots;i++) {
		 if(isItATheoryCourse(a[i]))
			 return i;
	 }
	 return -1;
 }
 
//it returns a slot number where a lab course is found in the Labslot
//returning -1 means no such theory couse is found in the labslot
int findALabCourseInTheorySolt(int  []a) {
	 for(int i=0;i<numberOfTheorySlots;i++) {
		 if(isItALabCourse(a[i]))
			 return i;
	 }
	 return -1;
}

//it returns a slot number where a free slot is found in the Labslot
//returning -1 means no such free slor is found in the labslot
int findAFreeSlotInLabSlot(int []a){
	 for(int i=numberOfTheorySlots;i<numberOfTheorySlots+numberOfLabSlots;i++) {
		 if(isItAFreeCourseTime(a[i]))
			 return i;
	 }
	 return -1;	
}


//it returns a slot number where a free slot is found in the theory slot
//returning -1 means no such free slot is found in the theory slot
int findAFreeSlotInTheorySlot(int []a){
	 for(int i=0;i<numberOfTheorySlots;i++) {
		 if(isItAFreeCourseTime(a[i]))
			 return i;
	 }
	 return -1;	
}

void swapingTwoSlots(Solution s, int slotNo1, int slotNo2){
	int temp = ((Permutation)s.getDecisionVariables()[0]).vector_[slotNo1];
	
	((Permutation)s.getDecisionVariables()[0]).vector_[slotNo1]=
			((Permutation)s.getDecisionVariables()[0]).vector_[slotNo2];
	
	((Permutation)s.getDecisionVariables()[0]).vector_[slotNo2]=temp;
}



  public void repair(Solution s) {
		
	 // long initTime = System.currentTimeMillis();
	  
	  //for all theory slots, search for a lab course that is misplaced
		for(int i=0; i<numberOfTheorySlots;i++) {
			
			int courseId = ((Permutation)s.getDecisionVariables()[0]).vector_[i];
			
			if(isItALabCourse(courseId)) {
				//lab course is found in theory slot
				
				//1. search for a theory course in lab slot to swap
				int slotNoWhereTheoryCouseNoInLabSlot = 
						findATheoryCourseInLabSolt(((Permutation)s.getDecisionVariables()[0]).vector_);
				
				if(slotNoWhereTheoryCouseNoInLabSlot!=-1) {
					//1.a. swap the theory and lab course
					swapingTwoSlots(s, i, slotNoWhereTheoryCouseNoInLabSlot);
				}else {
					//2. find a free slot in lab slot
					int freeSlotInLabSlot = 
							findAFreeSlotInLabSlot(((Permutation)s.getDecisionVariables()[0]).vector_);
					//1.a. swap the theory with a free slot
					swapingTwoSlots(s, i, freeSlotInLabSlot);
				}
			}
		}
		
		//for all lab slots, search for a theory course that is misplaced
		for(int i=numberOfTheorySlots; i<numberOfTheorySlots+numberOfLabSlots;i++) {
			
			int courseId = ((Permutation)s.getDecisionVariables()[0]).vector_[i];
			
			if(isItATheoryCourse(courseId)) {
				//theory course is found in lab slot
				
				//1. search for a lab course in theory slot to swap
				int slotNoWhereLabCouseNoInTheorySlot = 
						findALabCourseInTheorySolt(((Permutation)s.getDecisionVariables()[0]).vector_);
				
				if(slotNoWhereLabCouseNoInTheorySlot!=-1) {
					//1.a. swap the theory and lab course
					swapingTwoSlots(s, i, slotNoWhereLabCouseNoInTheorySlot);
				}else {
					//2. find a free slot in theory slot
					int freeSlotInTheorySlot = 
							findAFreeSlotInTheorySlot(((Permutation)s.getDecisionVariables()[0]).vector_);
					//1.a. swap the lab to a free slot
					swapingTwoSlots(s, i, freeSlotInTheorySlot);
				}
			}
		}
		
		//long estimatedTime = System.currentTimeMillis() - initTime;
	    //System.out.println("Time (repair): "+estimatedTime);
	  
	
		//checking(s);
  }
  
  void checking(Solution s) {
	  for(int i=0; i<numberOfTheorySlots;i++) {
			
			int courseId = ((Permutation)s.getDecisionVariables()[0]).vector_[i];
			
			try {
				if(isItALabCourse(courseId)) 
					throw new JMException("Error found in reresentation");
			}catch(JMException e) {
				e.printStackTrace();
			}
  }
		//for all lab slots, search for a theory course that is misplaced
		for(int i=numberOfTheorySlots; i<numberOfTheorySlots+numberOfLabSlots;i++) {
			
			int courseId = ((Permutation)s.getDecisionVariables()[0]).vector_[i];
			try {
						
			if(isItATheoryCourse(courseId)) {
				throw new JMException("Error found in reresentation");
			}
			}catch(JMException e) {
				e.printStackTrace();
			}
  }
		}






} // TSP
