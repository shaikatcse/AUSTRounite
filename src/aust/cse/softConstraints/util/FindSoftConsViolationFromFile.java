package aust.cse.softConstraints.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;
import aust.cse.routine.util.TeacherAssignedCourseInfo;
import jmetal.core.Solution;
import jmetal.encodings.variable.Permutation;
import jmetal.util.Configuration;

class TeacherPreferredSlot {
	Date startTime, endTime;

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public TeacherPreferredSlot(Date startTime, Date endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public TeacherPreferredSlot(String startTime, String endTime) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		try {
			this.startTime = format.parse(startTime);
			this.endTime = format.parse(endTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			this.startTime = new Date(Long.MAX_VALUE);
			this.endTime = new Date(Long.MAX_VALUE);
		}
	}

}

class TeacherPreferredSlotsInfo {

	final int assistantProfConsMultiplier = 13;
	final int associateProfConsMultiplier = assistantProfConsMultiplier + 16;
	final int profConsMultiplier = assistantProfConsMultiplier + 5;

	ArrayList<TeacherPreferredSlot> teacherPreferredSlots;
	int numberOfTimeTheCourseIsOutsidePreferredSlot;

	TeacherPreferredSlotsInfo() {
		teacherPreferredSlots = new ArrayList<TeacherPreferredSlot>();
		numberOfTimeTheCourseIsOutsidePreferredSlot = 0;
	}

	public TeacherPreferredSlotsInfo(ArrayList<TeacherPreferredSlot> teacherPreferredSlot, int numberOfConflicts) {
		this.teacherPreferredSlots = teacherPreferredSlot;
		this.numberOfTimeTheCourseIsOutsidePreferredSlot = numberOfConflicts;
	}

	Date convertStringToDate(String aDate) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date returnDate = null;
		try {
			returnDate = format.parse(aDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnDate;
	}

	void isTheClassTimeWithinThePreferredTimeSlot(SlotInfo aSlot) {

		Date slotEndTime = convertStringToDate(aSlot.getEndTime());
		Date slotStartTime = convertStringToDate(aSlot.getStartTime());
		for (int i = 0; i < teacherPreferredSlots.size(); i++) {
			TeacherPreferredSlot aPreferredSlot = teacherPreferredSlots.get(i);
			if (slotEndTime.before(aPreferredSlot.getStartTime()) || slotStartTime.after(aPreferredSlot.getEndTime())) {
				this.numberOfTimeTheCourseIsOutsidePreferredSlot++;
				break;
			}
		}
	}

	void isTheClassTimeWithinThePreferredTimeSlot(SlotInfo aSlot, String teacherName,
			HashMap<String, String> teacherInfo, HashMap<String, Integer> numberOfViolation) {
		String designation = teacherInfo.get(teacherName);

		Date slotEndTime = convertStringToDate(aSlot.getEndTime());
		Date slotStartTime = convertStringToDate(aSlot.getStartTime());
		for (int i = 0; i < teacherPreferredSlots.size(); i++) {
			TeacherPreferredSlot aPreferredSlot = teacherPreferredSlots.get(i);
			if (slotEndTime.before(aPreferredSlot.getStartTime()) || slotStartTime.after(aPreferredSlot.getEndTime())) {
				if (designation.equals("Assistant professor")) {
					numberOfViolation.put("Assistant professor", numberOfViolation.get("Assistant professor") + 1);
					break;
				} else if (designation.equals("Associate professor")) {
					numberOfViolation.put("Associate professor", numberOfViolation.get("Associate professor") + 1);
					break;
				} else if (designation.equals("Professor")) {
					numberOfViolation.put("Professor", numberOfViolation.get("Professor") + 1);
					break;
				} else {
					numberOfViolation.put("Lecturer", numberOfViolation.get("Lecturer") + 1);
					break;
				}

			}
		}
	}

}

public class FindSoftConsViolationFromFile {

	final int numberOfObjectives = 2;

	final int numberOfTheorySlots = 378;
	final int numberOfLabSlots = 144;

	File VariableFile;

	HashMap<String, Integer> numberOfviolationForDesignation;

	HashMap<String, String> teacherInfo;
	ArrayList<TeacherAssignedCourseInfo> teacherAssignedCourseInfo;
	HashSet<String> teacherNamesWhoGivenPrefererdTiming;
	MultiKeyMap<MultiKey, TeacherPreferredSlotsInfo> mapForCalculatingConstraintsOfTeacherPreferredSlots;

	ArrayList<CourseInfo> courseInfo;
	ArrayList<SlotInfo> slotInfo;

	void initializeNumberOfViolationForDesignation() {
		numberOfviolationForDesignation.clear();
		numberOfviolationForDesignation.put("Lecturer", 0);
		numberOfviolationForDesignation.put("Assistant professor", 0);
		numberOfviolationForDesignation.put("Associate professor", 0);
		numberOfviolationForDesignation.put("Professor", 0);

	}

	public FindSoftConsViolationFromFile(String path, String varFileName) {

		numberOfviolationForDesignation = new HashMap();

		teacherNamesWhoGivenPrefererdTiming = new HashSet();
		mapForCalculatingConstraintsOfTeacherPreferredSlots = new MultiKeyMap();

		// read courseInfo from courInfo.csv file
		readCourseInfoFromFile();

		// read slotInfo from slotInfo.csv file
		readSlotInfoFromFile();

		readTeacherAssignedCourseInfoFromFile();

		readTeacherPrefferedTimeSlotFromFile();

		readTeacherInfoFromFile();

		List<int[]> variableRow = readVariablesFromFile(path + varFileName);
		SlotInfo slotInfo;
		CourseInfo courseInfo;

		initializeNumberOfViolationForDesignation();

		try {
			/* Open the file */
			FileOutputStream fos = new FileOutputStream(path+"\\SoftConstViolationForDesignation");
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			for (int i = 0; i < variableRow.size(); i++) {
				int variables[] = variableRow.get(i);
				for (int j = 0; j < variables.length; j++) {
					slotInfo = SlotInfo.searchSlotInfoArryList(this.slotInfo, j + "");
					courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo, variables[j]);
					updateAllMaps(slotInfo, courseInfo);

				}

				printNumberOfViolation();
				writeViolationForDesignation( bw);
				initializeNumberOfViolationForDesignation();
			}
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}

	}

	

	public void writeViolationForDesignation( BufferedWriter bw) throws IOException {

		String[] designations = { "Professor", "Associate professor", "Assistant professor", "Lecturer" };

		for (String designation : designations) {
			bw.write(designation + ": "+numberOfviolationForDesignation.get(designation)+", ");
		}
		bw.newLine();

		/* Close the file */
	} // printObjectivesToFile

	void printNumberOfViolation() {
		String[] designations = { "Professor", "Associate professor", "Assistant professor", "Lecturer" };
		for (String designation : designations) {
			System.out.println(designation + ": " + numberOfviolationForDesignation.get(designation));
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
				int[] row = new int[info.length];
				for (int i = 0; i < info.length; i++) {
					row[i] = Integer.parseInt(info[i]);
				}
				variableRow.add(row);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return variableRow;

	}

	public void updateAllMaps(SlotInfo slotInfo, CourseInfo courseInfo) {
		if (!courseInfo.getCourseNo().equals("freetime")) {
			ArrayList<String> teacherNames = getTeacherNameAssignedForACourse(courseInfo);
			if (teacherNames != null) {
				for (String name : teacherNames) {
					if (teacherNamesWhoGivenPrefererdTiming.contains(name)) {
						MultiKey key = new MultiKey(name, slotInfo.getDay());
						if (!mapForCalculatingConstraintsOfTeacherPreferredSlots.containsKey(key)) {
							TeacherPreferredSlot tpSlot = new TeacherPreferredSlot("undefined", "undefined");
							TeacherPreferredSlotsInfo tpSlotInfo = new TeacherPreferredSlotsInfo();
							tpSlotInfo.teacherPreferredSlots.add(tpSlot);
							tpSlotInfo.isTheClassTimeWithinThePreferredTimeSlot(slotInfo, name, teacherInfo,
									numberOfviolationForDesignation);
							mapForCalculatingConstraintsOfTeacherPreferredSlots.put(key, tpSlotInfo);
						} else {
							TeacherPreferredSlotsInfo tpSlotInfo = mapForCalculatingConstraintsOfTeacherPreferredSlots
									.get(key);
							tpSlotInfo.isTheClassTimeWithinThePreferredTimeSlot(slotInfo, name, teacherInfo,
									numberOfviolationForDesignation);
						}
					}
				}
			}
		}

	}

	ArrayList<String> getTeacherNameAssignedForACourse(CourseInfo courseInfo) {
		ArrayList<String> teacherNames = new ArrayList<String>();

		for (int i = 0; i < teacherAssignedCourseInfo.size(); i++) {
			if (teacherAssignedCourseInfo.get(i).getAssignedCourseNo().equals(courseInfo.getCourseNo())
					&& teacherAssignedCourseInfo.get(i).getAssignedCourseSection()
							.equals(courseInfo.getAssignedSection())
					&& teacherAssignedCourseInfo.get(i).getAssignedCourseStudentGroup()
							.equals(courseInfo.getAssignedLabSection()))
				teacherNames.add(teacherAssignedCourseInfo.get(i).getTeacherName());
		}
		if (teacherNames.size() > 0)
			return teacherNames;
		else {
			return null;
		}
	}

	void readTeacherAssignedCourseInfoFromFile() {
		BufferedReader br = null;
		String line = "";
		String csvFile = "TeacherAssingedCourseInfo.csv";
		String cvsSplitBy = ",";

		teacherAssignedCourseInfo = new ArrayList<TeacherAssignedCourseInfo>();
		try {
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);

				TeacherAssignedCourseInfo teacherInfo = new TeacherAssignedCourseInfo(info[0], info[1], info[2],
						info[3]);

				teacherAssignedCourseInfo.add(teacherInfo);
			}
		} catch (IOException e) {
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

				CourseInfo infoClass = new CourseInfo(Integer.parseInt(info[0]), info[1], info[2], info[3], info[4],
						info[5]);

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

				SlotInfo infoSlot = new SlotInfo(info[0], info[1], info[2], info[3], info[4], info[5], info[6],
						info[7]);

				slotInfo.add(infoSlot);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	void readTeacherInfoFromFile() {
		BufferedReader br = null;
		String line = "";
		String csvFile = "TeacherInfo.csv";
		String cvsSplitBy = ",";

		teacherInfo = new HashMap<String, String>();
		try {
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);
				teacherInfo.put(info[0], info[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readTeacherPrefferedTimeSlotFromFile() {
		BufferedReader br = null;
		String line = "";
		String csvFile = "TeacherPreferredSlot.csv";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);

				// 0 -> name
				// 1 -> day
				// 2 -> startTime
				// 3 -> endTime
				teacherNamesWhoGivenPrefererdTiming.add(info[0]);

				MultiKey key = new MultiKey(info[0], info[1]);
				if (!mapForCalculatingConstraintsOfTeacherPreferredSlots.containsKey(key)) {
					TeacherPreferredSlotsInfo tpSlotInfo = new TeacherPreferredSlotsInfo();
					TeacherPreferredSlot tpSlot = new TeacherPreferredSlot(info[2], info[3]);
					tpSlotInfo.teacherPreferredSlots.add(tpSlot);
					mapForCalculatingConstraintsOfTeacherPreferredSlots.put(key, tpSlotInfo);
				} else {
					TeacherPreferredSlotsInfo tpSlotInfo = mapForCalculatingConstraintsOfTeacherPreferredSlots.get(key);
					TeacherPreferredSlot tpSlot = new TeacherPreferredSlot(info[2], info[3]);
					tpSlotInfo.teacherPreferredSlots.add(tpSlot);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String agrs[]) {
		// new DatabaseFromFile(".\\results\\VAR_Elitist_0");
		// new
		// FindSoftConsViolationFromFile(".\\results\\multiObjective\\NoSoftConstBinaryTournament\\run0\\FIS_VAR_NSGAII");
		new FindSoftConsViolationFromFile(
				".\\results\\multiObjective\\SmartInitWithSCBinaryTournament\\run0","\\FIS_VAR_NSGAII");

	}

}
