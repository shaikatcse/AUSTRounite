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

package aust.cse.routine.problem.singleObjective;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.Permutation;
import jmetal.util.JMException;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;

/**
 * Class representing a TSP (Traveling Salesman Problem) problem.
 */
public class AUSTCSERoutineProblem extends Problem {

	final int numberOfTheorySlots = 378;
	final int numberOfLabSlots = 144;

	Connection dbConnection;

	ArrayList<CourseInfo> courseInfo;
	ArrayList<SlotInfo> slotInfo;

	public Connection connectDatabase() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager
					.getConnection("jdbc:sqlite:C:\\Users\\shaik\\Desktop\\TimeTableTestDB3_backup.sqlite");
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public double calculateObjective1() {
		double obj1 = 0.0;

		String query = "SELECT DISTINCT Course.ysID, Timeslot.Day, MIN(time(Timeslot.startTime)) as Starting_Time, MAX(time(Timeslot.endTime)) as Finishing_Time, CourseClassroomTimeslot.Section, CourseClassroomTimeslot.StudentGroup, ((MAX(strftime('%s', Timeslot.endTime)) - MIN(strftime('%s', Timeslot.startTime)))/3600.0) as duration, count(Timeslot.Day) from Timeslot, CourseClassroomTimeslot, Course where Timeslot.slotID = CourseClassroomTimeslot.slotID and CourseClassroomTimeslot.CourseNo = Course.CourseNo "
				+ "GROUP BY Timeslot.DAY,CourseClassroomTimeslot.Section,Course.ysID, CourseClassroomTimeslot.StudentGroup "
				+ "ORDER BY Course.ysID ASC, CourseClassroomTimeslot.section ASC, case when Day= 'Saturday' then 1 when Day= 'Sunday' then 2 when Day= 'Monday' then 3 "
				+ "when Day= 'Tuesday' then 4 when Day= 'Wednesday' then 5 " + "else 6 end asc, time(startTime) ASC";

		try {
			PreparedStatement statement = dbConnection.prepareStatement(query);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// System.out.println("duration" + rs.getString("duration") );
				obj1 += Double.parseDouble(rs.getString("duration"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj1;
	}

	public int calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab() {
		int numberOfRow = 0;

		// Not more than three theory class and a lab in a day for A1/B1/C1

		String query1 = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Quantity"
				+ "from TimeSlot T, CourseClassroomTimeslot CCT, Course C"
				+ "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and CCT.StudentGroup<>2"
				+ "GROUP BY T.DAY,CCT.Section,ysID" + "having count(CCT.CourseNo) >4"
				+ "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1" + "when Day= 'Sunday' then 2"
				+ "when Day= 'Monday' then 3" + "when Day= 'Tuesday' then 4" + "when Day= 'Wednesday' then 5"
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
		String query2 = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Quantity"
				+ "from TimeSlot T, CourseClassroomTimeslot CCT, Course C"
				+ "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and CCT.StudentGroup<>1"
				+ "GROUP BY T.DAY,CCT.Section,ysID" + "having count(CCT.CourseNo) >4"
				+ "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1" + "when Day= 'Sunday' then 2"
				+ "when Day= 'Monday' then 3" + "when Day= 'Tuesday' then 4" + "when Day= 'Wednesday' then 5"
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
		int numberOfRow = 0;
		// Not more than two labs in a day for students of A1/B1/C1
		String query1 = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Lab_Quantity"
				+ "from TimeSlot T, CourseClassroomTimeslot CCT, Course C"
				+ "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and C.CourseType = 'Lab' and CCT.StudentGroup<>2"
				+ "GROUP BY T.DAY,CCT.Section,ysID" + "having count(C.CourseNo)>2"
				+ "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1" + "when Day= 'Sunday' then 2"
				+ "when Day= 'Monday' then 3" + "when Day= 'Tuesday' then 4" + "when Day= 'Wednesday' then 5"
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
		String query2 = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Lab_Quantity"
				+ "from TimeSlot T, CourseClassroomTimeslot CCT, Course C"
				+ "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and C.CourseType = 'Lab' and CCT.StudentGroup<>1"
				+ "GROUP BY T.DAY,CCT.Section,ysID" + "having count(C.CourseNo)>2"
				+ "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1" + "when Day= 'Sunday' then 2"
				+ "when Day= 'Monday' then 3" + "when Day= 'Tuesday' then 4" + "when Day= 'Wednesday' then 5"
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
		int numberOfRow=0;
		
		String query = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count( Distinct Cl.RoomNo) as Room_Quantity, CCT.RoomNo"
				     + "from TimeSlot T, CourseClassroomTimeslot CCT, Course C, Classroom Cl"
				     + "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and CCT.RoomNo = Cl.RoomNo and C.CourseType = 'Theory'" 
				     + "GROUP BY T.DAY,CCT.Section,ysID"
				     + "having count(Distinct Cl.RoomNo)>=2"
				     + "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1"
				     + "when Day= 'Sunday' then 2"
				     + "when Day= 'Monday' then 3"
				     + "when Day= 'Tuesday' then 4"
				     + "when Day= 'Wednesday' then 5" 
				     + "else 6"
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
		int numberOfRow=0;
		
		String query = "select DISTINCT C.ysID, T.Day, MIN(time(T.startTime)) as Starting_Time, MAX(time(T.endTime)) as Finishing_Time, CCT.Section,( MAX(time(T.endTime)) -MIN(time(T.startTime))) as difference, count(C.CourseNo) as Theory_Quantity"
				     + "from TimeSlot T, CourseClassroomTimeslot CCT, Course C"
				     + "where T.slotID = CCT.slotID and CCT.CourseNo = C.CourseNo and C.CourseType = 'Theory'" 
				     + "GROUP BY T.DAY,CCT.Section,ysID"
				     + "having count(C.CourseNo) >4 or count(C.CourseNo) <2"
				     + "ORDER BY C.ysID ASC, CCT.section ASC, case when Day= 'Saturday' then 1"
				     + "when Day= 'Sunday' then 2"
				     + "when Day= 'Monday' then 3"
				     + "when Day= 'Tuesday' then 4"
				     + "when Day= 'Wednesday' then 5" 
				     + "else 6"
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
	 * 
	 * @param filename
	 *            The file containing the definition of the problem
	 */
	public AUSTCSERoutineProblem(String solutionType) {
		numberOfVariables_ = 1;
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;
		problemName_ = "AUSTCSERoutineProblem";

		solutionType_ = new PermutationSolutionType(this);

		length_ = new int[numberOfVariables_];

		// connect the database
		dbConnection = connectDatabase();

		// read courseInfo from courInfo.csv file
		readCourseInfoFromFile();

		// read slotInfo from slotInfo.csv file
		readSlotInfoFromFile();
		try {
			if (solutionType.compareTo("Permutation") == 0)
				solutionType_ = new PermutationSolutionType(this);
			else {
				throw new JMException("Solution type invalid");
			}
		} catch (JMException e) {
			e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
		}

		System.out.println(522);
		length_[0] = 522;
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

				CourseInfo infoClass = new CourseInfo(Integer.parseInt(info[0]), info[1], info[2], info[3], info[4]);

				courseInfo.add(infoClass);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readSlotInfoFromFile() {
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

				SlotInfo infoSlot = new SlotInfo(info[0], info[1], info[2], info[3], info[4], info[5], info[6]);

				slotInfo.add(infoSlot);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void insertARowToCourseClassroomTimeslotTable(CourseInfo courseInfo, SlotInfo slotInfo) {
		String query = "insert into CourseClassroomTimeslot values (" + "\"" + courseInfo.getCourseNo() + "\"," + "\""
				+ slotInfo.getSlotID() + "\"," + "\"" + slotInfo.getRoomNo() + "\"," + "\""
				+ courseInfo.getAssignedSection() + "\"," + "\"" + courseInfo.getAssignedLabSection() + "\")";
		try {
			PreparedStatement statement = dbConnection.prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void fillClassroomTimeslotTable(Solution solution) {
		SlotInfo slotInfo;
		CourseInfo courseInfo;

		int length = ((Permutation) solution.getDecisionVariables()[0]).vector_.length;

		String bigInsertQuery = "INSERT INTO 'CourseClassroomTimeslot'";
		// for(int i=0; i<length; i++) {

		for (int i = 0; i < 500; i++) {

			slotInfo = SlotInfo.srachSlotInfoArryList(this.slotInfo, i + "");
			courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo,
					((Permutation) solution.getDecisionVariables()[0]).vector_[i]);

			if (i == 0) {
				bigInsertQuery = bigInsertQuery + " SELECT '" + courseInfo.getCourseNo() + "' AS 'courseNo', '"
						+ slotInfo.getSlotID() + "' AS 'slotId', '" + slotInfo.getRoomNo() + "' AS 'RoomNo', '"
						+ courseInfo.getAssignedSection() + "' AS 'Section' ,'" + courseInfo.getAssignedLabSection()
						+ "' AS 'StudentGroup'";
			} else {
				bigInsertQuery = bigInsertQuery + " UNION ALL SELECT '" + courseInfo.getCourseNo() + "', '"
						+ slotInfo.getSlotID() + "', '" + slotInfo.getRoomNo() + "', '"
						+ courseInfo.getAssignedSection() + "' ,'" + courseInfo.getAssignedLabSection() + "'";
			}

		}
		// System.out.println(bigInsertQuery);
		try {
			PreparedStatement statement = dbConnection.prepareStatement(bigInsertQuery);
			statement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bigInsertQuery = "INSERT INTO 'CourseClassroomTimeslot'";
		;

		for (int i = 500; i < length; i++) {

			slotInfo = SlotInfo.srachSlotInfoArryList(this.slotInfo, i + "");
			courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo,
					((Permutation) solution.getDecisionVariables()[0]).vector_[i]);

			// fill the table
			// insertARowToCourseClassroomTimeslotTable(courseInfo, slotInfo);

			if (i == 500) {
				bigInsertQuery = bigInsertQuery + " SELECT '" + courseInfo.getCourseNo() + "' AS 'courseNo', '"
						+ slotInfo.getSlotID() + "' AS 'slotId', '" + slotInfo.getRoomNo() + "' AS 'RoomNo', '"
						+ courseInfo.getAssignedSection() + "' AS 'Section' ,'" + courseInfo.getAssignedLabSection()
						+ "' AS 'StudentGroup'";
			} else {
				bigInsertQuery = bigInsertQuery + " UNION ALL SELECT '" + courseInfo.getCourseNo() + "', '"
						+ slotInfo.getSlotID() + "', '" + slotInfo.getRoomNo() + "', '"
						+ courseInfo.getAssignedSection() + "' ,'" + courseInfo.getAssignedLabSection() + "'";
			}

		}
		// System.out.println(bigInsertQuery);
		try {
			PreparedStatement statement = dbConnection.prepareStatement(bigInsertQuery);
			statement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Evaluates a solution
	 * 
	 * @param solution
	 *            The solution to evaluate
	 */
	public void evaluate(Solution solution) {
		double fitness;

		// delete the table first
		try {
			long initTime = System.currentTimeMillis();

			PreparedStatement statement = dbConnection.prepareStatement("delete from CourseClassroomTimeslot");
			statement.executeUpdate();
			long estimatedTime = System.currentTimeMillis() - initTime;
			System.out.println("Time (evaluate:deletion): " + estimatedTime);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long initTime = System.currentTimeMillis();

		fillClassroomTimeslotTable(solution);
		long estimatedTime = System.currentTimeMillis() - initTime;
		System.out.println("Time (evaluate:fill table): " + estimatedTime);

		initTime = System.currentTimeMillis();
		double obj1 = calculateObjective1();
		estimatedTime = System.currentTimeMillis() - initTime;
		System.out.println("Time (evaluate:): " + estimatedTime);

		solution.setObjective(0, obj1);

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
		
		int calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInADay = calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInADay();
		if(calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInADay>0) {
			totalConstraints += (-1*calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInADay);
			numberOfConstraints ++;
		}

		int calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay
				= calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay();
		if(calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay>0) {
			totalConstraints += (-1*calculateConstarintForNotMoreThan4TheoryClassesOrLessthan2TheoryClassesInADay);
			numberOfConstraints++;
		}
		
		solution.setOverallConstraintViolation(totalConstraints);
		solution.setNumberOfViolatedConstraint(numberOfConstraints);
	}

	
	boolean isItATheoryCourse(int courseId) {
		CourseInfo aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
		if (aCourse.getCourseType().equals("Theory"))
			return true;
		else
			return false;

	}

	boolean isItALabCourse(int courseId) {
		CourseInfo aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
		if (aCourse.getCourseType().equals("Lab"))
			return true;
		else
			return false;

	}

	boolean isItAFreeCourseTime(int courseId) {
		CourseInfo aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
		if (aCourse.getCourseNo().equals("freetime"))
			return true;
		else
			return false;

	}

	// it returns a slot number where a theory course is found in the Labslot
	// returning -1 means no such theory couse is found in the labslot
	int findATheoryCourseInLabSolt(int[] a) {
		for (int i = numberOfTheorySlots; i < numberOfTheorySlots + numberOfLabSlots; i++) {
			if (isItATheoryCourse(a[i]))
				return i;
		}
		return -1;
	}

	// it returns a slot number where a lab course is found in the Labslot
	// returning -1 means no such theory couse is found in the labslot
	int findALabCourseInTheorySolt(int[] a) {
		for (int i = 0; i < numberOfTheorySlots; i++) {
			if (isItALabCourse(a[i]))
				return i;
		}
		return -1;
	}

	// it returns a slot number where a free slot is found in the Labslot
	// returning -1 means no such free slor is found in the labslot
	int findAFreeSlotInLabSlot(int[] a) {
		for (int i = numberOfTheorySlots; i < numberOfTheorySlots + numberOfLabSlots; i++) {
			if (isItAFreeCourseTime(a[i]))
				return i;
		}
		return -1;
	}

	// it returns a slot number where a free slot is found in the theory slot
	// returning -1 means no such free slot is found in the theory slot
	int findAFreeSlotInTheorySlot(int[] a) {
		for (int i = 0; i < numberOfTheorySlots; i++) {
			if (isItAFreeCourseTime(a[i]))
				return i;
		}
		return -1;
	}

	void swapingTwoSlots(Solution s, int slotNo1, int slotNo2) {
		int temp = ((Permutation) s.getDecisionVariables()[0]).vector_[slotNo1];

		((Permutation) s.getDecisionVariables()[0]).vector_[slotNo1] = ((Permutation) s
				.getDecisionVariables()[0]).vector_[slotNo2];

		((Permutation) s.getDecisionVariables()[0]).vector_[slotNo2] = temp;
	}

	public void repair(Solution s) {

		long initTime = System.currentTimeMillis();

		// for all theory slots, search for a lab course that is misplaced
		for (int i = 0; i < numberOfTheorySlots; i++) {

			int courseId = ((Permutation) s.getDecisionVariables()[0]).vector_[i];

			if (isItALabCourse(courseId)) {
				// lab course is found in theory slot

				// 1. search for a theory course in lab slot to swap
				int slotNoWhereTheoryCouseNoInLabSlot = findATheoryCourseInLabSolt(
						((Permutation) s.getDecisionVariables()[0]).vector_);

				if (slotNoWhereTheoryCouseNoInLabSlot != -1) {
					// 1.a. swap the theory and lab course
					swapingTwoSlots(s, i, slotNoWhereTheoryCouseNoInLabSlot);
				} else {
					// 2. find a free slot in lab slot
					int freeSlotInLabSlot = findAFreeSlotInLabSlot(((Permutation) s.getDecisionVariables()[0]).vector_);
					// 1.a. swap the theory with a free slot
					swapingTwoSlots(s, i, freeSlotInLabSlot);
				}
			}
		}

		// for all lab slots, search for a theory course that is misplaced
		for (int i = numberOfTheorySlots; i < numberOfTheorySlots + numberOfLabSlots; i++) {

			int courseId = ((Permutation) s.getDecisionVariables()[0]).vector_[i];

			if (isItATheoryCourse(courseId)) {
				// theory course is found in lab slot

				// 1. search for a lab course in theory slot to swap
				int slotNoWhereLabCouseNoInTheorySlot = findALabCourseInTheorySolt(
						((Permutation) s.getDecisionVariables()[0]).vector_);

				if (slotNoWhereLabCouseNoInTheorySlot != -1) {
					// 1.a. swap the theory and lab course
					swapingTwoSlots(s, i, slotNoWhereLabCouseNoInTheorySlot);
				} else {
					// 2. find a free slot in theory slot
					int freeSlotInTheorySlot = findAFreeSlotInTheorySlot(
							((Permutation) s.getDecisionVariables()[0]).vector_);
					// 1.a. swap the lab to a free slot
					swapingTwoSlots(s, i, freeSlotInTheorySlot);
				}
			}
		}

		long estimatedTime = System.currentTimeMillis() - initTime;
		System.out.println("Time (repair): " + estimatedTime);

		checking(s);
	}

	void checking(Solution s) {
		for (int i = 0; i < numberOfTheorySlots; i++) {

			int courseId = ((Permutation) s.getDecisionVariables()[0]).vector_[i];

			try {
				if (isItALabCourse(courseId))
					throw new JMException("Error found in reresentation");
			} catch (JMException e) {
				e.printStackTrace();
			}
		}
		// for all lab slots, search for a theory course that is misplaced
		for (int i = numberOfTheorySlots; i < numberOfTheorySlots + numberOfLabSlots; i++) {

			int courseId = ((Permutation) s.getDecisionVariables()[0]).vector_[i];
			try {

				if (isItATheoryCourse(courseId)) {
					throw new JMException("Error found in reresentation");
				}
			} catch (JMException e) {
				e.printStackTrace();
			}
		}
	}

} // TSP
