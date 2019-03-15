package aust.cse.routine.problem.multiObjective.withoutDataBase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;
import aust.cse.routine.util.TeacherAssignedCourseInfo;

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
	
	final int assistantProfConsMultiplier=13;
	final int associateProfConsMultiplier= assistantProfConsMultiplier + 16;
	final int profConsMultiplier= assistantProfConsMultiplier + 5;
	
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
	
	void isTheClassTimeWithinThePreferredTimeSlot(SlotInfo aSlot, String teacherName, HashMap<String, String> teacherInfo) {
		String designation = teacherInfo.get(teacherName);
		
		Date slotEndTime = convertStringToDate(aSlot.getEndTime());
		Date slotStartTime = convertStringToDate(aSlot.getStartTime());
		for (int i = 0; i < teacherPreferredSlots.size(); i++) {
			TeacherPreferredSlot aPreferredSlot = teacherPreferredSlots.get(i);
			if (slotEndTime.before(aPreferredSlot.getStartTime()) || slotStartTime.after(aPreferredSlot.getEndTime())) {
				if(designation.equals("Assistant professor")) {
					this.numberOfTimeTheCourseIsOutsidePreferredSlot += assistantProfConsMultiplier;
					break;
				}else if(designation.equals("Associate professor")) {
					this.numberOfTimeTheCourseIsOutsidePreferredSlot += associateProfConsMultiplier;
					break;
				}else if(designation.equals("Professor")) {
					this.numberOfTimeTheCourseIsOutsidePreferredSlot += profConsMultiplier;
					break;
				}else {
					this.numberOfTimeTheCourseIsOutsidePreferredSlot ++;
					break;
				}
				
			}
		}
	}

}

public class ModellingSoftConstraints {
	ArrayList<TeacherAssignedCourseInfo> teacherAssignedCourseInfo;
	HashSet<String> teacherNamesWhoGivenPrefererdTiming;

	HashMap<String, String> teacherInfo;
	
	MultiKeyMap<MultiKey, TeacherPreferredSlotsInfo> mapForCalculatingConstraintsOfTeacherPreferredSlots;

	public ModellingSoftConstraints() {
		mapForCalculatingConstraintsOfTeacherPreferredSlots = new MultiKeyMap();
		teacherNamesWhoGivenPrefererdTiming = new HashSet();
		readTeacherPrefferedTimeSlotFromFile();
		readTeacherAssignedCourseInfoFromFile();
	}

	public void clearAllMaps() {
		for (MultiKey key : mapForCalculatingConstraintsOfTeacherPreferredSlots.keySet()) {
			TeacherPreferredSlotsInfo tpSlotInfo = mapForCalculatingConstraintsOfTeacherPreferredSlots.get(key);
			tpSlotInfo.numberOfTimeTheCourseIsOutsidePreferredSlot=0;
		}
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
							tpSlotInfo.isTheClassTimeWithinThePreferredTimeSlot(slotInfo, name, teacherInfo);
							mapForCalculatingConstraintsOfTeacherPreferredSlots.put(key, tpSlotInfo);
						} else {
							TeacherPreferredSlotsInfo tpSlotInfo = mapForCalculatingConstraintsOfTeacherPreferredSlots
									.get(key);
							tpSlotInfo.isTheClassTimeWithinThePreferredTimeSlot(slotInfo, name, teacherInfo);
						}
					}
				}
			}
		}

	}

	public double calculateConstraintsOfTeacherPreferredSlots() {
		double constraintViolation = 0.0;
		for (MultiKey key : mapForCalculatingConstraintsOfTeacherPreferredSlots.keySet()) {
			TeacherPreferredSlotsInfo tpSlotInfo = mapForCalculatingConstraintsOfTeacherPreferredSlots.get(key);
			constraintViolation += tpSlotInfo.numberOfTimeTheCourseIsOutsidePreferredSlot;
		}
		return constraintViolation;

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

	Date convertStringToDate(String date) {

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		try {
			return format.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String agrs[]) {
		ModellingSoftConstraints msc = new ModellingSoftConstraints();

	}
}
