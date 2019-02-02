package aust.cse.routine.problem.multiObjective.withoutDataBase;

import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

//this is improved because it considers all the lab sections (A1, A2, ..)
class StudentTimingConsideringLabSections {
	Date startTimeLabSec1, endTimeLabSec1, startTimeLabSec2, endTimeLabSec2;

	public StudentTimingConsideringLabSections(Date startTimeLabSec1, Date endTimeLabSec1, Date startTimeLabSec2,
			Date endTimeLabSec2) {
		super();
		this.startTimeLabSec1 = startTimeLabSec1;
		this.endTimeLabSec1 = endTimeLabSec1;
		this.startTimeLabSec2 = startTimeLabSec2;
		this.endTimeLabSec2 = endTimeLabSec2;
	}

	public Date getStartTimeLabSec1() {
		return startTimeLabSec1;
	}

	public void setStartTimeLabSec1(Date startTimeLabSec1) {
		this.startTimeLabSec1 = startTimeLabSec1;
	}

	public Date getEndTimeLabSec1() {
		return endTimeLabSec1;
	}

	public void setEndTimeLabSec1(Date endTimeLabSec1) {
		this.endTimeLabSec1 = endTimeLabSec1;
	}

	public Date getStartTimeLabSec2() {
		return startTimeLabSec2;
	}

	public void setStartTimeLabSec2(Date startTimeLabSec2) {
		this.startTimeLabSec2 = startTimeLabSec2;
	}

	public Date getEndTimeLabSec2() {
		return endTimeLabSec2;
	}

	public void setEndTimeLabSec2(Date endTimeLabSec2) {
		this.endTimeLabSec2 = endTimeLabSec2;
	}

}

public class ModellingObjectivesV2 {

	CourseInfo courseInfo;
	SlotInfo slotInfo;

	// Calculate student time
	MultiKeyMap<MultiKey, StudentTimingConsideringLabSections> mapForStudentTiming;

	// calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab
	MultiKeyMap<MultiKey, Integer> mapForNoOfTheoryForStudent, mapForNoOfLabForStudent;

	// calculateConstraintsForNotMoreThanTwoLabsInADayForStudents

	public ModellingObjectivesV2() {
		this.mapForStudentTiming = new MultiKeyMap<MultiKey, StudentTimingConsideringLabSections>();
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
		Date startTimeLabSec1 = null;
		Date startTimeLabSec2 = null;
		
		try {
			startTimeLabSec1 = format.parse(slotInfo.getStartTime());
			startTimeLabSec2 = format.parse(slotInfo.getStartTime());
			
				
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cal.setTime(startTimeLabSec1);
		Date endTimeLabSec1 = null;
		Date endTimeLabSec2 = null;
		

		if (!courseInfo.getCourseNo().equals("freetime")) {
			if (!mapForStudentTiming.containsKey(key)) {
				// if the key is not contained in the map, so new entry

				if (courseInfo.getCourseType().equals("Theory")) {
					// add 50 minute to start time
					cal.add(Calendar.MINUTE, 50);
					endTimeLabSec1 = cal.getTime();
					StudentTimingConsideringLabSections st = new StudentTimingConsideringLabSections(
							startTimeLabSec1, endTimeLabSec1,startTimeLabSec1, endTimeLabSec1);
					mapForStudentTiming.put(key, st);
				} else if (courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("3")) {
					// add 150 mintues to start time
					cal.add(Calendar.MINUTE, 150);
					endTimeLabSec1 = cal.getTime();
					StudentTimingConsideringLabSections st = new StudentTimingConsideringLabSections(
							startTimeLabSec1, endTimeLabSec1,startTimeLabSec1, endTimeLabSec1);
					mapForStudentTiming.put(key, st);
				}else if (courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("1")) {
					// add 150 mintues to start time
					cal.add(Calendar.MINUTE, 150);
					endTimeLabSec1 = cal.getTime();
					StudentTimingConsideringLabSections st = new StudentTimingConsideringLabSections(
							startTimeLabSec1, endTimeLabSec1,null, null);
					mapForStudentTiming.put(key, st);
				}else if (courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("1")) {
					// add 150 mintues to start time
					cal.add(Calendar.MINUTE, 150);
					endTimeLabSec2 = cal.getTime();
					StudentTimingConsideringLabSections st = new StudentTimingConsideringLabSections(
							null, null, startTimeLabSec2, endTimeLabSec2);
					mapForStudentTiming.put(key, st);
				}

				
			} else {
				// if the key is already contained, we need to update start and end time
				if (courseInfo.getCourseType().equals("Theory")) {
					// if this is a theory
					cal.add(Calendar.MINUTE, 50);
					endTimeLabSec1 = cal.getTime();
					endTimeLabSec2 = cal.getTime();

					// extract current start and end time
					StudentTimingConsideringLabSections currentTime = mapForStudentTiming.get(key);
					Date currentStartTimeLabSec1 = currentTime.getStartTimeLabSec1();
					Date currentEndTimeLabSec1 = currentTime.getEndTimeLabSec1();
					Date currentStartTimeLabSec2 = currentTime.getStartTimeLabSec2();
					Date currentEndTimeLabSec2 = currentTime.getEndTimeLabSec2();
					
					//////////////////////////////////////////////

					if (WhichTimeIsBefore(startTimeLabSec1, currentStartTimeLabSec1)) {
						// if start time of the slot is before current start time
						currentTime.setStartTimeLabSec1(startTimeLabSec1);
						currentTime.setStartTimeLabSec2(startTimeLabSec2);
						
					}
					if (WhichTimeIsAfter(endTimeLabSec1, currentEndTimeLabSec1)) {
						// if end time of the slot is after current end time
						currentTime.setEndTimeLabSec1(currentEndTimeLabSec1);
						currentTime.setEndTimeLabSec2(currentEndTimeLabSec2);
						
					}

				} else if (courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("3")) {
					// if this is a Lab, but 0.75 cr lab, so add 150 with both of them; very simiral to theory
					cal.add(Calendar.MINUTE, 150);
					endTimeLabSec1 = cal.getTime();
					endTimeLabSec2 = cal.getTime();

					// extract current start and end time
					StudentTimingConsideringLabSections currentTime = mapForStudentTiming.get(key);
					Date currentStartTimeLabSec1 = currentTime.getStartTimeLabSec1();
					Date currentEndTimeLabSec1 = currentTime.getEndTimeLabSec1();
					Date currentStartTimeLabSec2 = currentTime.getStartTimeLabSec2();
					Date currentEndTimeLabSec2 = currentTime.getEndTimeLabSec2();
					
					//////////////////////////////////////////////

					if (WhichTimeIsBefore(startTimeLabSec1, currentStartTimeLabSec1)) {
						// if start time of the slot is before current start time
						currentTime.setStartTimeLabSec1(startTimeLabSec1);
						currentTime.setStartTimeLabSec2(startTimeLabSec2);
						
					}
					if (WhichTimeIsAfter(endTimeLabSec1, currentEndTimeLabSec1)) {
						// if end time of the slot is after current end time
						currentTime.setEndTimeLabSec1(currentEndTimeLabSec1);
						currentTime.setEndTimeLabSec2(currentEndTimeLabSec2);
						
					}

				}else if(courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("1")){
					//if this is a lab, but only with lab section 1.
					cal.add(Calendar.MINUTE, 150);
					endTimeLabSec1 = cal.getTime();
					
					// extract current start and end time
					StudentTimingConsideringLabSections currentTime = mapForStudentTiming.get(key);
					Date currentStartTimeLabSec1 = currentTime.getStartTimeLabSec1();
					Date currentEndTimeLabSec1 = currentTime.getEndTimeLabSec1();
					///////////////////////////////////////////////////////////////
					
					if (WhichTimeIsBefore(startTimeLabSec1, currentStartTimeLabSec1)) {
						// if start time of the slot is before current start time
						currentTime.setStartTimeLabSec1(startTimeLabSec1);
					}
					
					if (WhichTimeIsAfter(endTimeLabSec1, currentEndTimeLabSec1)) {
						// if end time of the slot is after current end time
						currentTime.setEndTimeLabSec1(currentEndTimeLabSec1);
					}
					
				}else if(courseInfo.getCourseType().equals("Lab") && courseInfo.getAssignedLabSection().equals("2")){
					//if this is a lab, but only with lab section 1.
					cal.add(Calendar.MINUTE, 150);
					endTimeLabSec2 = cal.getTime();
					
					// extract current start and end time
					StudentTimingConsideringLabSections currentTime = mapForStudentTiming.get(key);
					Date currentStartTimeLabSec2 = currentTime.getStartTimeLabSec2();
					Date currentEndTimeLabSec2 = currentTime.getEndTimeLabSec2();
					///////////////////////////////////////////////////////////////
					
					if (WhichTimeIsBefore(startTimeLabSec2, currentStartTimeLabSec2)) {
						// if start time of the slot is before current start time
						currentTime.setStartTimeLabSec2(startTimeLabSec2);
					}
					
					if (WhichTimeIsAfter(endTimeLabSec2, currentEndTimeLabSec2)) {
						// if end time of the slot is after current end time
						currentTime.setEndTimeLabSec2(currentEndTimeLabSec2);
					}
					
				}

			}
		}
	}

	double calculateTotalTime() {
		long totalTime = 0;
		for (MultiKey key : mapForStudentTiming.keySet()) {
			StudentTimingConsideringLabSections st = mapForStudentTiming.get(key);
			totalTime += (st.endTimeLabSec1.getTime() - st.startTimeLabSec1.getTime());
			//totalTime += (st.endTimeLabSec2.getTime() - st.startTimeLabSec2.getTime());
		}

		PrintAllInfo();

		return ((totalTime / (1000 * 60 * 60)));
	}

	void PrintAllInfo() {

		class Key implements Comparable<Key> {
			String yearSemester, section, day;

			public Key(String yearSemester, String section, String day) {
				super();
				this.yearSemester = yearSemester;
				this.section = section;
				this.day = day;
			}

			public String getYearSemester() {
				return yearSemester;
			}

			public void setYearSemester(String yearSemester) {
				this.yearSemester = yearSemester;
			}

			public String getSection() {
				return section;
			}

			public void setSection(String section) {
				this.section = section;
			}

			public String getDay() {
				return day;
			}

			public void setDay(String day) {
				this.day = day;
			}

			@Override
			public int compareTo(Key o1) {
				int value1 = this.getYearSemester().compareTo(o1.getYearSemester());
				if (value1 == 0) {
					int value2 = this.getSection().compareTo(o1.getSection());
					if (value2 == 0) {
						try {
							SimpleDateFormat format = new SimpleDateFormat("EEE");
							Date d1 = format.parse(this.getDay());
							Date d2 = format.parse(o1.getDay());
							Calendar cal1 = Calendar.getInstance();
							Calendar cal2 = Calendar.getInstance();
							cal1.setTime(d1);
							cal2.setTime(d2);
							return cal1.get(Calendar.DAY_OF_WEEK) - cal2.get(Calendar.DAY_OF_WEEK);
						} catch (ParseException pe) {
							throw new RuntimeException(pe);
						}
					} else {
						return value2;
					}

				}

				return value1;
			}

		}

		List sortedKeys = new ArrayList<Key>();
		for (MultiKey key : mapForStudentTiming.keySet()) {
			String ys = key.getKey(0).toString();
			String sec = key.getKey(1).toString();
			String day = key.getKey(2).toString();

			Key k = new Key(ys, sec, day);
			sortedKeys.add(k);
		}

		Collections.sort(sortedKeys);

		for (int i = 0; i < sortedKeys.size(); i++) {
			Key key = (Key) sortedKeys.get(i);

			StudentTimingConsideringLabSections st = mapForStudentTiming
					.get(new MultiKey(key.getYearSemester(), key.getSection(), key.getDay()));
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

			System.out.println(key.getYearSemester() + " " + key.getSection() + " " + key.getDay() + " "
					+ dateFormat.format(st.startTimeLabSec1) + " " + dateFormat.format(st.endTimeLabSec1));

			System.out.println(key.getYearSemester() + " " + key.getSection() + " " + key.getDay() + " "
					+ dateFormat.format(st.startTimeLabSec2) + " " + dateFormat.format(st.endTimeLabSec2));
		}
	}

}
