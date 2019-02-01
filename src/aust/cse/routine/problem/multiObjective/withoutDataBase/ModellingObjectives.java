package aust.cse.routine.problem.multiObjective.withoutDataBase;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

class StudentTiming {
	Date startTime, endTime;

	StudentTiming(Date startTime, Date endTime) {
		this.startTime = startTime;
		this.endTime = endTime;

	}

	Date getStartTime() {
		return startTime;
	}

	Date getEndTime() {
		return endTime;
	}

	void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
}

public class ModellingObjectives {

	CourseInfo courseInfo;
	SlotInfo slotInfo;

	// Calculate student time
	MultiKeyMap<MultiKey, StudentTiming> mapForStudentTiming;

	// calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab
	MultiKeyMap<MultiKey, Integer> mapForNoOfTheoryForStudent, mapForNoOfLabForStudent;

	// calculateConstraintsForNotMoreThanTwoLabsInADayForStudents

	public ModellingObjectives() {
		this.mapForStudentTiming = new MultiKeyMap<MultiKey, StudentTiming>();
	}

	boolean WhichTimeIsBefore(Date t1, Date t2) {
		// return true is t1 is before, else return false
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(t1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(t2);
		boolean b = cal1.before(cal2);
		
		return b;

	}

	boolean WhichTimeIsAfter(Date t1, Date t2) {
		// return true is t1 is before, else return false
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(t1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(t2);

		return cal1.after(cal2);

	}

	void fillUpTheMap(SlotInfo slotInfo, CourseInfo courseInfo) {
		MultiKey key = new MultiKey(courseInfo.getCourseYearSemester(), courseInfo.getAssignedSection(),
				slotInfo.getDay());
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date startTime = null;
		try {
			startTime = format.parse(slotInfo.getStartTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cal.setTime(startTime);
		Date endTime = null;

		if (!courseInfo.getCourseNo().equals("freetime")) {
			if (!mapForStudentTiming.containsKey(key)) {
				// if the key is not contained in the map, so new entry

				if (courseInfo.getCourseType().equals("Theory")) {
					// add 50 minute to start time
					cal.add(Calendar.MINUTE, 50);
					endTime = cal.getTime();
				} else if (courseInfo.getCourseType().equals("Lab")) {
					// add 150 mintues to start time
					cal.add(Calendar.MINUTE, 150);
					endTime = cal.getTime();
				}

				StudentTiming st = new StudentTiming(startTime, endTime);
				mapForStudentTiming.put(key, st);
			} else {
				// if the key is already contained, we need to update start and end time
				if (courseInfo.getCourseType().equals("Theory")) {
					// if this is a theory
					cal.add(Calendar.MINUTE, 50);
					endTime = cal.getTime();

					// extract current start and end time
					StudentTiming currentTime = mapForStudentTiming.get(key);
					Date currentStartTime = currentTime.getStartTime();
					Date currentEndTime = currentTime.getEndTime();
					//////////////////////////////////////////////

					if (WhichTimeIsBefore(startTime, currentStartTime)) {
						// if start time of the slot is before current start time
						currentTime.setStartTime(startTime);
					}
					if (WhichTimeIsAfter(endTime, currentEndTime)) {
						// if end time of the slot is after current end time
						currentTime.setEndTime(endTime);
					}

				} else if (courseInfo.getCourseType().equals("Lab")) {
					// if this is a theory
					cal.add(Calendar.MINUTE, 150);
					endTime = cal.getTime();

					// extract current start and end time
					StudentTiming currentTime = mapForStudentTiming.get(key);
					Date currentStartTime = currentTime.getStartTime();
					Date currentEndTime = currentTime.getEndTime();
					//////////////////////////////////////////////

					if (WhichTimeIsBefore(startTime, currentStartTime)) {
						// if start time of the slot is before current start time
						currentTime.setStartTime(startTime);
					}
					if (WhichTimeIsAfter(endTime, currentEndTime)) {
						// if end time of the slot is after current end time
						currentTime.setEndTime(endTime);
					}
				}

			}
		}
	}

	double calculateTotalTime() {
		long totalTime=0;
		for (MultiKey key : mapForStudentTiming.keySet()) {
			StudentTiming st = mapForStudentTiming.get(key);
			totalTime += (st.endTime.getTime() - st.startTime.getTime());
		}
		return  ((totalTime / (1000*60*60)));
	}
}
