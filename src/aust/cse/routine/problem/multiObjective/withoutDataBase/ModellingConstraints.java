package aust.cse.routine.problem.multiObjective.withoutDataBase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;
import aust.cse.routine.util.TeacherAssignedCourseInfo;

class TeacherClassTiming {
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

	public TeacherClassTiming(Date startTime, Date endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public TeacherClassTiming(String startTime, String endTime) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		try {
			this.startTime = format.parse(startTime);
			this.endTime = format.parse(endTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class TeacherClassTimingForConstraintHandeling {
	ArrayList<TeacherClassTiming> teacherClassTiming;
	int numberOfConflicts;

	TeacherClassTimingForConstraintHandeling() {
		teacherClassTiming = new ArrayList<TeacherClassTiming>();
		numberOfConflicts = 0;
	}

	public TeacherClassTimingForConstraintHandeling(ArrayList<TeacherClassTiming> teacherClassTiming,
			int numberOfConflicts) {
		this.teacherClassTiming = teacherClassTiming;
		this.numberOfConflicts = numberOfConflicts;
	}

	ArrayList<TeacherClassTiming> getTeacherTiming() {
		return teacherClassTiming;
	}

}

class StudentClassTiming {
	Date startTime, endTime;

	String section;
	// section can be: full, Lab1, Lab2
	String labSection;
	
	String day;
	
	

	public String getLabSection() {
		return labSection;
	}

	public void setLabSection(String labSection) {
		this.labSection = labSection;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

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

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public StudentClassTiming(Date startTime, Date endTime, String section, String labSection, String day) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.section = section;
		this.labSection = labSection;
		this.day = day;
	}

	public StudentClassTiming(String startTime, String endTime, String section, String labSection, String day) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		try {
			this.startTime = format.parse(startTime);
			this.endTime = format.parse(endTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.section = section;
		this.labSection = labSection;
		this.day = day;
	}

}

class StudentClassTimingForConstraintHandeling {
	ArrayList<StudentClassTiming> studentClassTiming;
	int numberOfConflicts;

	StudentClassTimingForConstraintHandeling() {
		studentClassTiming = new ArrayList<StudentClassTiming>();
		numberOfConflicts = 0;
	}

	public StudentClassTimingForConstraintHandeling(ArrayList<StudentClassTiming> studentClassTiming,
			int numberOfConflicts) {
		this.studentClassTiming = studentClassTiming;
		this.numberOfConflicts = numberOfConflicts;
	}

	ArrayList<StudentClassTiming> getStudentTiming() {
		return studentClassTiming;
	}

}

public class ModellingConstraints {
	ArrayList<TeacherAssignedCourseInfo> teacherAssignedCourseInfo;

	// calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab
	MultiKeyMap<MultiKey, Integer> mapForNoOfTheoryForStudent, mapForNoOfLabForStudent;
	MultiKeyMap<MultiKey, HashSet<String>> mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay;

	MultiKeyMap<MultiKey, TeacherClassTimingForConstraintHandeling> mapForCalculatingConstraintsOfSameTimeClassForATeacher;
	MultiKeyMap<MultiKey, StudentClassTimingForConstraintHandeling> mapForCalculatingConstraintsOfSameTimeClassForAStudent;

	public ModellingConstraints() {
		mapForNoOfTheoryForStudent = new MultiKeyMap<MultiKey, Integer>();
		mapForNoOfLabForStudent = new MultiKeyMap<MultiKey, Integer>();
		mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay = new MultiKeyMap<MultiKey, HashSet<String>>();
		mapForCalculatingConstraintsOfSameTimeClassForATeacher = new MultiKeyMap<MultiKey, TeacherClassTimingForConstraintHandeling>();
		mapForCalculatingConstraintsOfSameTimeClassForAStudent = new MultiKeyMap<MultiKey, StudentClassTimingForConstraintHandeling>();
		readTeacherAssignedCourseInfoFromFile();
	}

	public void clearAllMaps() {
		mapForNoOfTheoryForStudent.clear();
		mapForNoOfLabForStudent.clear();
		mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay.clear();
		mapForCalculatingConstraintsOfSameTimeClassForATeacher.clear();
		mapForCalculatingConstraintsOfSameTimeClassForAStudent.clear();

	}

	public void updateAllMaps(SlotInfo slotInfo, CourseInfo courseInfo) {
		// constraints: calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab:
		if (!courseInfo.getCourseNo().equals("freetime")) {
			if (courseInfo.getCourseType().equals("Theory")) {
				// key for 1 section, ex; A1, B1, ..
				MultiKey keyForTheoryCourseCountSec1 = new MultiKey(courseInfo.getCourseYearSemester(),
						courseInfo.getAssignedSection(), "1", slotInfo.getDay());
				// key for 2 section, ex; A2, B2, ..
				MultiKey keyForTheoryCourseCountSec2 = new MultiKey(courseInfo.getCourseYearSemester(),
						courseInfo.getAssignedSection(), "2", slotInfo.getDay());

				if (!mapForNoOfTheoryForStudent.containsKey(keyForTheoryCourseCountSec1)) {
					mapForNoOfTheoryForStudent.put(keyForTheoryCourseCountSec1, 1);
				} else {
					Integer numberOfTheory = mapForNoOfTheoryForStudent.get(keyForTheoryCourseCountSec1) + 1;
					mapForNoOfTheoryForStudent.put(keyForTheoryCourseCountSec1, numberOfTheory);
				}

				if (!mapForNoOfTheoryForStudent.containsKey(keyForTheoryCourseCountSec2)) {
					mapForNoOfTheoryForStudent.put(keyForTheoryCourseCountSec2, 1);
				} else {
					Integer numberOfTheory = mapForNoOfTheoryForStudent.get(keyForTheoryCourseCountSec2) + 1;
					mapForNoOfTheoryForStudent.put(keyForTheoryCourseCountSec2, numberOfTheory);
				}
			}
			if ((courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("1"))
					|| (courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("2"))) {
				MultiKey keyForLabCourseCount = new MultiKey(courseInfo.getCourseYearSemester(),
						courseInfo.getAssignedSection(), courseInfo.getAssignedLabSection(), slotInfo.getDay());

				if (!mapForNoOfLabForStudent.containsKey(keyForLabCourseCount)) {
					mapForNoOfLabForStudent.put(keyForLabCourseCount, 1);
				} else {
					Integer numberOfLab = mapForNoOfLabForStudent.get(keyForLabCourseCount) + 1;
					mapForNoOfLabForStudent.put(keyForLabCourseCount, numberOfLab);
				}
			} else if (courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("3")) {

				MultiKey keyForLabCourseCountSec1 = new MultiKey(courseInfo.getCourseYearSemester(),
						courseInfo.getAssignedSection(), "1", slotInfo.getDay());

				MultiKey keyForLabCourseCountSec2 = new MultiKey(courseInfo.getCourseYearSemester(),
						courseInfo.getAssignedSection(), "2", slotInfo.getDay());

				if (!mapForNoOfLabForStudent.containsKey(keyForLabCourseCountSec1)) {
					mapForNoOfLabForStudent.put(keyForLabCourseCountSec1, 1);
				} else {
					Integer numberOfLab = mapForNoOfLabForStudent.get(keyForLabCourseCountSec1) + 1;
					mapForNoOfLabForStudent.put(keyForLabCourseCountSec1, numberOfLab);
				}

				if (!mapForNoOfLabForStudent.containsKey(keyForLabCourseCountSec2)) {
					mapForNoOfLabForStudent.put(keyForLabCourseCountSec2, 1);
				} else {
					Integer numberOfLab = mapForNoOfLabForStudent.get(keyForLabCourseCountSec2) + 1;
					mapForNoOfLabForStudent.put(keyForLabCourseCountSec2, numberOfLab);
				}

			}

		}

		// constraint:
		// calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay
		if (!courseInfo.getCourseNo().equals("freetime")) {
			if (courseInfo.getCourseType().equals("Theory")) {
				MultiKey keyForRoomSession = new MultiKey(slotInfo.getRoomNo(), slotInfo.getDay(),
						slotInfo.getSession());
				if (!mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay
						.containsKey(keyForRoomSession)) {
					HashSet<String> yearSemInASession = new HashSet();
					yearSemInASession.add(courseInfo.getCourseYearSemester() + courseInfo.getAssignedSection());
					mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay
							.put(keyForRoomSession, yearSemInASession);
				} else {
					HashSet<String> yearSemInASession = mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay
							.get(keyForRoomSession);
					yearSemInASession.add(courseInfo.getCourseYearSemester() + courseInfo.getAssignedSection());
				}
			}
		}

		// constarints: calculateConstarintsForNoDoubleClassForSameTeacherInSAmeTIme
		if (!courseInfo.getCourseNo().equals("freetime")) {
			ArrayList<String> teacherNames = getTeacherNameAssignedForACourse(courseInfo);
			if (teacherNames != null) {
				for (int i = 0; i < teacherNames.size(); i++) {
					MultiKey keyForTeacherClassTiming = new MultiKey(teacherNames.get(i), slotInfo.getDay());
					if (!mapForCalculatingConstraintsOfSameTimeClassForATeacher.containsKey(keyForTeacherClassTiming)) {
						ArrayList<TeacherClassTiming> teacherClassTiming = new ArrayList<>();
						teacherClassTiming.add(new TeacherClassTiming(slotInfo.getStartTime(), slotInfo.getEndTime()));
						TeacherClassTimingForConstraintHandeling tConstraint = new TeacherClassTimingForConstraintHandeling(
								teacherClassTiming, 0);
						mapForCalculatingConstraintsOfSameTimeClassForATeacher.put(keyForTeacherClassTiming,
								tConstraint);

					} else {
						TeacherClassTimingForConstraintHandeling tConstraint = mapForCalculatingConstraintsOfSameTimeClassForATeacher
								.get(keyForTeacherClassTiming);
						ArrayList<TeacherClassTiming> teacherClassTiming = tConstraint.getTeacherTiming();
						int currentNoOfteacherTimingSlot = teacherClassTiming.size();
						boolean trackLoop = true;
						for (int j = 0; j < currentNoOfteacherTimingSlot; j++) {
							if (detectConflict(teacherClassTiming.get(j), slotInfo)) {
								tConstraint.numberOfConflicts += 1;
								updateTeacherClassTiming(teacherClassTiming);
								trackLoop = false;
								break;

							}

						}
						if (trackLoop) {
							// add the timeslot
							teacherClassTiming
									.add(new TeacherClassTiming(slotInfo.getStartTime(), slotInfo.getEndTime()));
							updateTeacherClassTiming(teacherClassTiming);

						}

					}
				}
			}
		}

		// constarints: calculateConstarintsForNoDoubleClassForSameStudentInSAmeTIme
		if (!courseInfo.getCourseNo().equals("freetime")) {
			String studentYearSemester = courseInfo.getCourseYearSemester();
			String studentSection = courseInfo.getAssignedSection();
			//3 -> full section, 1 -> A1/B1/C1, 2 -> A2/B2/C2
			String studentLabSection = courseInfo.getAssignedLabSection();
			MultiKey keyForStudentClassTiming = new MultiKey(studentYearSemester, studentSection, slotInfo.getDay());
			if (!mapForCalculatingConstraintsOfSameTimeClassForAStudent.containsKey(keyForStudentClassTiming)) {
				ArrayList<StudentClassTiming> studentClassTiming = new ArrayList<>();
				studentClassTiming
						.add(new StudentClassTiming(slotInfo.getStartTime(), slotInfo.getEndTime(), studentSection, studentLabSection, slotInfo.getDay()));
				StudentClassTimingForConstraintHandeling sConstraint = new StudentClassTimingForConstraintHandeling(
						studentClassTiming, 0);
				mapForCalculatingConstraintsOfSameTimeClassForAStudent.put(keyForStudentClassTiming, sConstraint);

			} else {
				StudentClassTimingForConstraintHandeling sConstraint = mapForCalculatingConstraintsOfSameTimeClassForAStudent
						.get(keyForStudentClassTiming);
				ArrayList<StudentClassTiming> studentClassTiming = sConstraint.getStudentTiming();
				int currentNoOfStudentTimingSlot = studentClassTiming.size();
				boolean trackLoop = true;
				for (int j = 0; j < currentNoOfStudentTimingSlot; j++) {
					if ((studentClassTiming.get(j).getLabSection().equals("3") && studentLabSection.equals("1"))
							|| (studentClassTiming.get(j).getLabSection().equals("1") && studentLabSection.equals("3"))
							|| (studentClassTiming.get(j).getLabSection().equals("3") && studentLabSection.equals("2"))
							|| (studentClassTiming.get(j).getLabSection().equals("2") && studentLabSection.equals("3"))
							|| (studentClassTiming.get(j).getLabSection().equals(studentLabSection))) {

						if (detectConflict(studentClassTiming.get(j), slotInfo)) {
							sConstraint.numberOfConflicts += 1;
							updateStudentClassTiming(studentClassTiming);
							trackLoop = false;
							break;

						}

					}
				}
				if (trackLoop) {
					// add the timeslot
					studentClassTiming.add(new StudentClassTiming(slotInfo.getStartTime(), slotInfo.getEndTime(), studentSection, studentLabSection, slotInfo.getDay()));
					updateStudentClassTiming(studentClassTiming);

				}

			}
		}

	}

	boolean detectConflict(StudentClassTiming tct, SlotInfo slotInfo) {
		Date s1StartTime = tct.getStartTime();
		Date s1EndTime = tct.getEndTime();
		double classTimeOfSlot1 = (s1EndTime.getTime() - s1StartTime.getTime()) / 60000.0;

		Date s2StartTime = convertStringToDate(slotInfo.getStartTime());
		Date s2EndTime = convertStringToDate(slotInfo.getEndTime());
		double classTImeSlot2 = (s2EndTime.getTime() - s2StartTime.getTime()) / 60000.0;

		if (classTimeOfSlot1 == classTImeSlot2) {
			// either these two slots are theory or lab
			if (s1StartTime.compareTo(s2StartTime) == 0)
				return true;
		} else if (classTimeOfSlot1 < classTImeSlot2) {
			if (s1StartTime.after(s2StartTime) && s1EndTime.before(s2EndTime)) {
				tct.setStartTime(s2StartTime);
				tct.setEndTime(s2EndTime);
				return true;
			} else if (s1StartTime.compareTo(s2StartTime) == 0) {
				tct.setStartTime(s2StartTime);
				tct.setEndTime(s2EndTime);
				return true;
			} else if (s1EndTime.compareTo(s2EndTime) == 0) {
				tct.setStartTime(s2StartTime);
				tct.setEndTime(s2EndTime);
				return true;
			}
		} else {
			if (s1StartTime.before(s2StartTime) && s1EndTime.after(s2EndTime)) {
				return true;
			} else if (s1StartTime.compareTo(s2StartTime) == 0) {
				return true;
			} else if (s1EndTime.compareTo(s2EndTime) == 0) {
				return true;
			}
		}
		return false;

	}

	boolean detectConflict(TeacherClassTiming tct, SlotInfo slotInfo) {
		Date s1StartTime = tct.getStartTime();
		Date s1EndTime = tct.getEndTime();
		double classTimeOfSlot1 = (s1EndTime.getTime() - s1StartTime.getTime()) / 60000.0;

		Date s2StartTime = convertStringToDate(slotInfo.getStartTime());
		Date s2EndTime = convertStringToDate(slotInfo.getEndTime());
		double classTImeSlot2 = (s2EndTime.getTime() - s2StartTime.getTime()) / 60000.0;

		if (classTimeOfSlot1 == classTImeSlot2) {
			// either these two slots are theory or lab
			if (s1StartTime.compareTo(s2StartTime) == 0)
				return true;
		} else if (classTimeOfSlot1 < classTImeSlot2) {
			if (s1StartTime.after(s2StartTime) && s1EndTime.before(s2EndTime)) {
				tct.setStartTime(s2StartTime);
				tct.setEndTime(s2EndTime);
				return true;
			} else if (s1StartTime.compareTo(s2StartTime) == 0) {
				tct.setStartTime(s2StartTime);
				tct.setEndTime(s2EndTime);
				return true;
			} else if (s1EndTime.compareTo(s2EndTime) == 0) {
				tct.setStartTime(s2StartTime);
				tct.setEndTime(s2EndTime);
				return true;
			}
		} else {
			if (s1StartTime.before(s2StartTime) && s1EndTime.after(s2EndTime)) {
				return true;
			} else if (s1StartTime.compareTo(s2StartTime) == 0) {
				return true;
			} else if (s1EndTime.compareTo(s2EndTime) == 0) {
				return true;
			}
		}
		return false;

	}

	//update for students
	void updateStudentClassTiming(ArrayList<StudentClassTiming> studentClassTiming) {
		boolean isAnItemDeleted = false;
		for (int i = 0; i < studentClassTiming.size(); i++) {
			for (int j = i + 1; j < studentClassTiming.size(); j++) {
				Date IStartTime = studentClassTiming.get(i).getStartTime();
				Date IEndTime = studentClassTiming.get(i).getEndTime();
				double classTimeOfI = (IEndTime.getTime() - IStartTime.getTime()) / 60000.0;

				Date JStartTime = studentClassTiming.get(j).getStartTime();
				Date JEndTime = studentClassTiming.get(j).getEndTime();
				double classTimeJ = (JEndTime.getTime() - JStartTime.getTime()) / 60000.0;

				if (classTimeOfI == classTimeJ) {
					// either these two slots are theory or lab
					if (IStartTime.compareTo(JStartTime) == 0) {
						studentClassTiming.remove(j);
						j--;
					}
				} else if (classTimeOfI < classTimeJ) {
					if (IStartTime.after(JStartTime) && IEndTime.before(JEndTime)) {
						studentClassTiming.remove(i);
						j--;
					} else if (IStartTime.compareTo(JStartTime) == 0) {
						studentClassTiming.remove(i);
						j--;
					} else if (IEndTime.compareTo(JEndTime) == 0) {
						studentClassTiming.remove(i);
						j--;
					}
				} else {
					if (IStartTime.before(JStartTime) && IEndTime.after(JEndTime)) {
						studentClassTiming.remove(j);
						j--;
					} else if (IStartTime.compareTo(JStartTime) == 0) {
						studentClassTiming.remove(j);
						j--;
					} else if (IEndTime.compareTo(JEndTime) == 0) {
						studentClassTiming.remove(j);
						j--;
					}
				}

			}
		}
	}
	
	//update for teacher
	void updateTeacherClassTiming(ArrayList<TeacherClassTiming> teacherClassTiming) {
		boolean isAnItemDeleted = false;
		for (int i = 0; i < teacherClassTiming.size(); i++) {
			for (int j = i + 1; j < teacherClassTiming.size(); j++) {
				Date IStartTime = teacherClassTiming.get(i).getStartTime();
				Date IEndTime = teacherClassTiming.get(i).getEndTime();
				double classTimeOfI = (IEndTime.getTime() - IStartTime.getTime()) / 60000.0;

				Date JStartTime = teacherClassTiming.get(j).getStartTime();
				Date JEndTime = teacherClassTiming.get(j).getEndTime();
				double classTimeJ = (JEndTime.getTime() - JStartTime.getTime()) / 60000.0;

				if (classTimeOfI == classTimeJ) {
					// either these two slots are theory or lab
					if (IStartTime.compareTo(JStartTime) == 0) {
						teacherClassTiming.remove(j);
						j--;
					}
				} else if (classTimeOfI < classTimeJ) {
					if (IStartTime.after(JStartTime) && IEndTime.before(JEndTime)) {
						teacherClassTiming.remove(i);
						j--;
					} else if (IStartTime.compareTo(JStartTime) == 0) {
						teacherClassTiming.remove(i);
						j--;
					} else if (IEndTime.compareTo(JEndTime) == 0) {
						teacherClassTiming.remove(i);
						j--;
					}
				} else {
					if (IStartTime.before(JStartTime) && IEndTime.after(JEndTime)) {
						teacherClassTiming.remove(j);
						j--;
					} else if (IStartTime.compareTo(JStartTime) == 0) {
						teacherClassTiming.remove(j);
						j--;
					} else if (IEndTime.compareTo(JEndTime) == 0) {
						teacherClassTiming.remove(j);
						j--;
					}
				}

			}
		}
	}

	public double calculatingConstraintsOfSameTimeClassForATeacher() {
		double constarintViolation = 0.0;
		for (MultiKey key : mapForCalculatingConstraintsOfSameTimeClassForATeacher.keySet()) {
			/*
			 * if(mapForCalculatingConstraintsOfSameTimeClassForATeacher.get(key).
			 * numberOfConflicts==1) { System.out.println("hhh"); }
			 */
			constarintViolation += mapForCalculatingConstraintsOfSameTimeClassForATeacher.get(key).numberOfConflicts;
		}
		return constarintViolation;
	}

	public double calculatingConstraintsOfSameTimeClassForAStudent() {
		double constarintViolation = 0.0;
		for (MultiKey key : mapForCalculatingConstraintsOfSameTimeClassForAStudent.keySet()) {
			/*
			 * if(mapForCalculatingConstraintsOfSameTimeClassForATeacher.get(key).
			 * numberOfConflicts==1) { System.out.println("hhh"); }
			 */
			constarintViolation += mapForCalculatingConstraintsOfSameTimeClassForAStudent.get(key).numberOfConflicts;
		}
		return constarintViolation;
	}

	public double calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay() {
		double constarintViolation = 0.0;
		for (MultiKey key : mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay
				.keySet()) {
			HashSet<String> hs = mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay
					.get(key);
			constarintViolation += (hs.size() - 1);
		}
		return constarintViolation;
	}

	public double calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab() {
		double constraintViolation = 0.0;
		for (MultiKey key : mapForNoOfTheoryForStudent.keySet()) {
			int noOfTheory = mapForNoOfTheoryForStudent.get(key);
			int noOfLab = 0;
			if (mapForNoOfLabForStudent.containsKey(key))
				noOfLab = mapForNoOfLabForStudent.get(key);

			if (noOfTheory > 4 && noOfLab > 1)
				constraintViolation += 1;
		}
		return constraintViolation;
	}

	public double calculateConstraintsNotMoreThanFourTheoryClassAndOneLab() {
		double constraintViolation = 0.0;
		for (MultiKey key : mapForNoOfTheoryForStudent.keySet()) {
			int noOfTheory = mapForNoOfTheoryForStudent.get(key);
			if (noOfTheory > 4)
				constraintViolation += 1;
		}
		return constraintViolation;
	}

	public double calculateConstraintsNotMoreThanTwoLabClassAndOneLab() {
		double constraintViolation = 0.0;
		for (MultiKey key : mapForNoOfLabForStudent.keySet()) {
			int noOfLab = mapForNoOfLabForStudent.get(key);
			if (noOfLab > 2)
				constraintViolation += 1;
		}
		return constraintViolation;
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

	ArrayList<String> getTeacherNameAssignedForACourse(CourseInfo courseInfo) {
		ArrayList<String> teacherNames = new ArrayList<String>();
		;
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
}
