package aust.cse.routine.problem.multiObjective.withoutDataBase;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;

public class ModellingConstraints {
	// calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab
		MultiKeyMap<MultiKey, Integer> mapForNoOfTheoryForStudent, mapForNoOfLabForStudent;
		MultiKeyMap<MultiKey, String[]> mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay;
		
		
		ModellingConstraints(){
			mapForNoOfTheoryForStudent = new MultiKeyMap<MultiKey, Integer>();
			mapForcalculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay = 
					new MultiKeyMap<MultiKey, String[]>;
		}
		
		void updateAllMaps(CourseInfo courseInfo, SlotInfo slotInfo) {
			//constraints
			if(!courseInfo.getCourseNo().equals("freetime")) {
				if (courseInfo.getCourseType().equals("Theory")) {
					MultiKey keyForTheoryCourseCount = new MultiKey(courseInfo.getCourseYearSemester(), courseInfo.getAssignedSection(), slotInfo.getDay());
					
				}
			}
		}
}
