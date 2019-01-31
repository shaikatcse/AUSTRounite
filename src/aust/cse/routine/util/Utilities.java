package aust.cse.routine.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import jmetal.core.Solution;
import jmetal.encodings.variable.Permutation;

public class Utilities {
	public static void solutionToDatabase(Solution s, String databasePath) {

		
		Connection dbConnection=null;
		try {
			Class.forName("org.sqlite.JDBC");
			dbConnection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

		} catch (Exception e) {
			e.printStackTrace();

		}

		BufferedReader br = null;
		String line = "";
		String csvFile = "CourseInfo.csv";
		String cvsSplitBy = ",";

		ArrayList<CourseInfo>courseInfoList = new ArrayList<CourseInfo>();
		try {
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);

				CourseInfo infoClass = new CourseInfo(Integer.parseInt(info[0]), info[1], info[2], info[3], info[4]);

				courseInfoList.add(infoClass);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		 br = null;
		 line = "";
		 csvFile = "SlotInfo.csv";
		 cvsSplitBy = ",";

		 ArrayList<SlotInfo> slotInfoList = new ArrayList<SlotInfo>();
			
		 try {
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);

				SlotInfo infoSlot = new SlotInfo(info[0], info[1], info[2], info[3], info[4], info[5], info[6]);

				slotInfoList.add(infoSlot);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

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

		SlotInfo slotInfo;
		CourseInfo courseInfo;

		int length = ((Permutation) s.getDecisionVariables()[0]).vector_.length;

		String bigInsertQuery = "INSERT INTO 'CourseClassroomTimeslot'";
		// for(int i=0; i<length; i++) {

		for (int i = 0; i < 500; i++) {

			slotInfo = SlotInfo.srachSlotInfoArryList(slotInfoList, i + "");
			courseInfo = CourseInfo.srachCourseInfoArryList(courseInfoList,
					((Permutation) s.getDecisionVariables()[0]).vector_[i]);

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

			slotInfo = SlotInfo.srachSlotInfoArryList(slotInfoList, i + "");
			courseInfo = CourseInfo.srachCourseInfoArryList(courseInfoList,
					((Permutation) s.getDecisionVariables()[0]).vector_[i]);

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
		}catch(NullPointerException e) {
			e.printStackTrace();
		}

	}
}
