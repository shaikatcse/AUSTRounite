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

class TeacherClassTiming{
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


class TeacherClassTimingForConstraintHandeling{
		ArrayList<TeacherClassTiming> teacherClassTiming;
		int numberOfConflicts;
		TeacherClassTimingForConstraintHandeling(){
			teacherClassTiming = new ArrayList<TeacherClassTiming>();
			numberOfConflicts = 0;
		}
		public TeacherClassTimingForConstraintHandeling(ArrayList<TeacherClassTiming> teacherClassTiming, int numberOfConflicts) {
			this.teacherClassTiming = teacherClassTiming;
			this.numberOfConflicts = numberOfConflicts;
		}
		
		ArrayList<TeacherClassTiming> getTeacherTiming(){
			return teacherClassTiming;
		}
		
}

public class ModellingConstraints {
	ArrayList<TeacherAssignedCourseInfo> teacherAssignedCourseInfo;
	
	// calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab
		MultiKeyMap<MultiKey, Integer> mapForNoOfTheoryForStudent, mapForNoOfLabForStudent;
		MultiKeyMap<MultiKey, HashSet<String>> mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay;
		
		MultiKeyMap<MultiKey, TeacherClassTimingForConstraintHandeling> mapForCalculatingConstraintsOfSameTimeClassForATeacher;
		
		public ModellingConstraints(){
			mapForNoOfTheoryForStudent = new MultiKeyMap<MultiKey, Integer>();
			mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay = 
					new MultiKeyMap<MultiKey, HashSet<String>>();
			mapForCalculatingConstraintsOfSameTimeClassForATeacher =
					new MultiKeyMap<MultiKey, TeacherClassTimingForConstraintHandeling>();
			readTeacherAssignedCourseInfoFromFile();
		}
		
		public void updateAllMaps(CourseInfo courseInfo, SlotInfo slotInfo) {
			//constraints: 	calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab:
			if(!courseInfo.getCourseNo().equals("freetime")) {
				if (courseInfo.getCourseType().equals("Theory")) {
					MultiKey keyForTheoryCourseCount = new MultiKey(courseInfo.getCourseYearSemester(), courseInfo.getAssignedSection(), slotInfo.getDay());
					if(!mapForNoOfTheoryForStudent.containsKey(keyForTheoryCourseCount)) {
						mapForNoOfTheoryForStudent.put(keyForTheoryCourseCount, 1);
					}else {
						Integer numberOfTheory = mapForNoOfTheoryForStudent.get(keyForTheoryCourseCount)+1;
						mapForNoOfTheoryForStudent.put(keyForTheoryCourseCount, numberOfTheory);
					}
				}
				if ( (courseInfo.getCourseType().equals("Lab") &&  courseInfo.getAssignedLabSection().equals("1"))
					|| (courseInfo.getCourseType().equals("Lab") &&  courseInfo.getAssignedLabSection().equals("2"))	) {
					MultiKey keyForLabCourseCount = new MultiKey(courseInfo.getCourseYearSemester(), courseInfo.getAssignedSection(), 
							courseInfo.getAssignedLabSection(), slotInfo.getDay());
					if(!mapForNoOfLabForStudent.containsKey(keyForLabCourseCount)) {
						mapForNoOfLabForStudent.put(keyForLabCourseCount, 1);
					}else {
						Integer numberOfLab = mapForNoOfTheoryForStudent.get(keyForLabCourseCount)+1;
						mapForNoOfLabForStudent.put(keyForLabCourseCount, numberOfLab);
					}
				}else if  (courseInfo.getCourseType().equals("Lab") &&  courseInfo.getAssignedLabSection().equals("3")) {
					MultiKey keyForLabCourseCountSec1 = new MultiKey(courseInfo.getCourseYearSemester(), courseInfo.getAssignedSection(), 
							"1", slotInfo.getDay());

					MultiKey keyForLabCourseCountSec2 = new MultiKey(courseInfo.getCourseYearSemester(), courseInfo.getAssignedSection(), 
							"2", slotInfo.getDay());
					
					if(!mapForNoOfLabForStudent.containsKey(keyForLabCourseCountSec1)) {
						mapForNoOfLabForStudent.put(keyForLabCourseCountSec1, 1);
					}else {
						Integer numberOfLab = mapForNoOfTheoryForStudent.get(keyForLabCourseCountSec1)+1;
						mapForNoOfLabForStudent.put(keyForLabCourseCountSec1, numberOfLab);
					}
					
					if(!mapForNoOfLabForStudent.containsKey(keyForLabCourseCountSec2)) {
						mapForNoOfLabForStudent.put(keyForLabCourseCountSec2, 1);
					}else {
						Integer numberOfLab = mapForNoOfTheoryForStudent.get(keyForLabCourseCountSec2)+1;
						mapForNoOfLabForStudent.put(keyForLabCourseCountSec2, numberOfLab);
					}
					
				}
				
			}
			
			//constraint: calculateConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay
			if(!courseInfo.getCourseNo().equals("freetime")) {
				if (courseInfo.getCourseType().equals("Theory")) {
					MultiKey keyForRoomSession = new MultiKey(slotInfo.getRoomNo(), slotInfo.getDay(), slotInfo.getSession());
					if(!mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay.containsKey(keyForRoomSession)) {
						HashSet<String> yearSemInASession = new HashSet();
						yearSemInASession.add(courseInfo.getCourseYearSemester());
						mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay.put(keyForRoomSession, yearSemInASession);
					}else {
						HashSet<String> yearSemInASession=mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay.
								get(keyForRoomSession);
						yearSemInASession.add(courseInfo.getCourseYearSemester());
					}
				}
			}
		
			//constarints: calculateConstarintsForNoDoubleClassForSameTeacherInSAmeTIme
			if(!courseInfo.getCourseNo().equals("freetime")) {
				ArrayList<String> teacherNames = getTeacherNameAssignedForACourse(courseInfo);
				for(int i=0;i<teacherNames.size();i++) {
					MultiKey keyForTeacherClassTiming = new MultiKey(teacherNames.get(i), slotInfo.getDay());
					if(!mapForCalculatingConstraintsOfSameTimeClassForATeacher.containsKey(keyForTeacherClassTiming)) {
						ArrayList<TeacherClassTiming> teacherClassTiming = new ArrayList<>();
						teacherClassTiming.add(new TeacherClassTiming(slotInfo.getStartTime(), slotInfo.getEndTime()));
						TeacherClassTimingForConstraintHandeling tConstraint = 
								new TeacherClassTimingForConstraintHandeling(teacherClassTiming, 0);
						
					}else {
						TeacherClassTimingForConstraintHandeling tConstraint = mapForCalculatingConstraintsOfSameTimeClassForATeacher
								.get(keyForTeacherClassTiming);
						ArrayList<TeacherClassTiming> teacherClassTiming = tConstraint.getTeacherTiming();
						for(int j=0;j<teacherClassTiming.size();j++) {
							if(detectConflict(teacherClassTiming.get(j), slotInfo)) {
								tConstraint.numberOfConflicts += 1;
								
							}else {
								// add the timeslot 
								teacherClassTiming.add(new TeacherClassTiming(slotInfo.getStartTime(), slotInfo.getEndTime()));
							}
						}
						
					}
				}
			}
		}
		
		boolean detectConflict(TeacherClassTiming tct, SlotInfo slotInfo) {
			Date s1StartTime = tct.getStartTime();
			Date s1EndTime = tct.getEndTime();
			double classTimeOfSlot1 = (s1EndTime.getTime() - s1StartTime.getTime())/60000.0;
			
			Date s2StartTime = convertStringToDate(slotInfo.getStartTime());
			Date s2EndTime = convertStringToDate(slotInfo.getEndTime());
			double classTImeSlot2 = (s2EndTime.getTime() - s2StartTime.getTime())/60000.0;
			
			if(classTimeOfSlot1 == classTImeSlot2) {
				//either these two slots are theory or lab
				if(s1StartTime.compareTo(s2StartTime)==0)
					return true;
			}else if(classTimeOfSlot1<classTImeSlot2) {
				if(s1StartTime.after(s2StartTime) && s1EndTime.before(s2EndTime)) {
					tct.setStartTime(s2StartTime);
					tct.setEndTime(s2EndTime);
					return true;
				}else if(s1StartTime.compareTo(s2StartTime)==0) {
					tct.setStartTime(s2StartTime);
					tct.setEndTime(s2EndTime);
					return true;
				}else if(s1EndTime.compareTo(s2EndTime)==0) {
					tct.setStartTime(s2StartTime);
					tct.setEndTime(s2EndTime);
					return true;
				}
			}else {
				if(s2StartTime.before(s1StartTime) && s2EndTime.after(s1EndTime)) {
					return true;
				}else if(s2StartTime.compareTo(s1StartTime)==0) {
					return true;
				}else if(s2EndTime.compareTo(s2EndTime)==0) {
					return true;
				}
			}
			return false;
			
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
			int i = 0;
			for (; i < teacherAssignedCourseInfo.size(); i++) {
				if (teacherAssignedCourseInfo.get(i).getAssignedCourseNo().equals(courseInfo.getCourseNo())
						&& teacherAssignedCourseInfo.get(i).getAssignedCourseSection()
								.equals(courseInfo.getAssignedSection())
						&& teacherAssignedCourseInfo.get(i).getAssignedCourseStudentGroup()
								.equals(courseInfo.getAssignedLabSection()))
					teacherNames.add(teacherAssignedCourseInfo.get(i).getTeacherName());
			}
			if (teacherNames.size() > 0)
				return teacherNames;
			else
				return null;
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
