package aust.cse.routine.problem.multiObjective.withoutDataBase;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;

public class ModellingConstraints {
	// calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab
		MultiKeyMap<MultiKey, Integer> mapForNoOfTheoryForStudent, mapForNoOfLabForStudent;
		MultiKeyMap<MultiKey, HashSet<String>> mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay;
		
	
		public ModellingConstraints(){
			mapForNoOfTheoryForStudent = new MultiKeyMap<MultiKey, Integer>();
			mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay = 
					new MultiKeyMap<MultiKey, HashSet<String>>();
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
		}
}
