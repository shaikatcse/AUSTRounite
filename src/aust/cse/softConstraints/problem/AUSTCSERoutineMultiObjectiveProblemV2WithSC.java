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

package aust.cse.softConstraints.problem;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.Permutation;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.io.*;
import java.nio.channels.FileLockInterruptionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.swing.JOptionPane;

import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingConstraints;
import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingObjectivesV2;
import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingSoftConstraints;
import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;
import aust.cse.routine.util.TeacherAssignedCourseInfo;

class SupportForCreatVariables {

	final static int numberOfTheorySlots = 378;
	final static int numberofTheoryCourses = 300;
	final static int numberOfLabSlots = 144;

	// saving teach and course info
	static ArrayList<TeacherAssignedCourseInfo> teacherAssignedCourseInfo;

	static boolean isTheTeacherFree(int[] vector, CourseInfo course, int currentSlotIndex,
			ArrayList<CourseInfo> courseInfo, ArrayList<SlotInfo> slotInfo) {

		ArrayList<String> nameOfTheTeacher = getTeacherNameAssignedForACourse(course);
		SlotInfo currentSlot = SlotInfo.searchSlotInfoArryList(slotInfo, currentSlotIndex + "");

		if (nameOfTheTeacher != null) {
			for (String teacherName : nameOfTheTeacher) {
				for (int i = 0; i < vector.length; i++) {
					if (i == currentSlotIndex || vector[i] == -1)
						continue;
					SlotInfo s = SlotInfo.searchSlotInfoArryList(slotInfo, i + "");
					CourseInfo courseInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
					ArrayList<String> nameOfTheTeacherInLoop = getTeacherNameAssignedForACourse(courseInLoop);
					if(nameOfTheTeacherInLoop != null) {
					for (String teacherNameInLoop : nameOfTheTeacherInLoop) {
						if (s.getDay().equals(currentSlot.getDay()) && teacherNameInLoop.equals(teacherName)) {
							if (detectConflict(s, currentSlot)) {
								return true;
							}
						}
					}

					}
				}
			}
		}
		return false;
	}

	static ArrayList<String> getTeacherNameAssignedForACourse(CourseInfo courseInfo) {
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

	static Date convertStringToDate(String date) {

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		try {
			return format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	static boolean detectConflict(SlotInfo tct, SlotInfo slotInfo) {
		Date s1StartTime = convertStringToDate(tct.getStartTime());
		Date s1EndTime = convertStringToDate(tct.getEndTime());
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
				return true;
			} else if (s1StartTime.compareTo(s2StartTime) == 0) {
				return true;
			} else if (s1EndTime.compareTo(s2EndTime) == 0) {
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

	static boolean areStdentFreeInALabTimeSlot(int[] vector, int slotHead, int dayInNumber, CourseInfo courseInSlotHead,
			ArrayList<CourseInfo> courseInfo, ArrayList<SlotInfo> slotInfo) {

		boolean areStudentFreeInTheorySlot = false;
		SlotInfo slotHeadInfo = SlotInfo.searchSlotInfoArryList(slotInfo, slotHead + "");
		for (int i = dayInNumber * 63; i < dayInNumber * 63 + 63 && !areStudentFreeInTheorySlot; i = i + 3) {
			int slotHeadInLoop = i;
			SlotInfo slotHeadInLoopInfo = SlotInfo.searchSlotInfoArryList(slotInfo, slotHeadInLoop + "");
			if (vector[slotHeadInLoop] != -1) {
				if (slotHeadInfo.getDay().equals(slotHeadInLoopInfo.getDay())
						&& slotHeadInfo.getStartTime().equals(slotHeadInLoopInfo.getStartTime())) {
					CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo,
							vector[slotHeadInLoop]);
					if (courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
							&& courseInSlotHead.getAssignedSection()
									.equals(courseInSlotHeadInLoop.getAssignedSection())) {
						areStudentFreeInTheorySlot = true;
					}

				}
			}

		}

		// this is for tracking any lab course is in the same time slot that conflicts
		// with the lab that we want to assigned
		boolean areStudentFreeLabSlot = false;
		for (int i = numberOfTheorySlots + dayInNumber * 24; i < numberOfTheorySlots + dayInNumber * 24 + 24; i++) {
			SlotInfo slotHeadInLoopInfo = SlotInfo.searchSlotInfoArryList(slotInfo, i + "");
			if (vector[i] != -1) {
				if (slotHeadInfo.getDay().equals(slotHeadInLoopInfo.getDay())
						&& slotHeadInfo.getStartTime().equals(slotHeadInLoopInfo.getStartTime())) {
					CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
					if (courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
							&& courseInSlotHead.getAssignedSection()
									.equals(courseInSlotHeadInLoop.getAssignedSection())) {
						areStudentFreeLabSlot = true;
					}

				}
			}
		}

		if (areStudentFreeInTheorySlot || areStudentFreeLabSlot)
			return false;
		else
			return true;
	}

	static boolean isThereMoreThan3TheoryAnd1LabInTheSameDay(int[] vector, int slotHead, int dayInNumber,
			CourseInfo courseInSlotHead, ArrayList<CourseInfo> courseInfo) {
		int counter = 0;
		for (int i = dayInNumber * 63; i < dayInNumber * 63 + 63; i = i + 3) {
			CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
			if (vector[i] != -1) {
				if (courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
						&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
					counter += 3;
				}
			}

		}

		for (int i = numberOfTheorySlots + dayInNumber * 24; i < numberOfTheorySlots + dayInNumber * 24 + 24; i++) {
			CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
			if (vector[i] != -1) {
				if (courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
						&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
					counter++;
				}
			}
		}
		if (counter >= 4)
			return false;
		else
			return true;
	}

	static boolean isThereMoreThan2LabsInTheSameDay(int[] vector, int slotHead, int dayInNumber,
			CourseInfo courseInSlotHead, ArrayList<CourseInfo> courseInfo) {
		int counter = 0;
		for (int i = numberOfTheorySlots + dayInNumber * 24; i < numberOfTheorySlots + dayInNumber * 24 + 24; i++) {
			CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
			if (vector[i] != -1) {
				if (courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
						&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
					counter++;
				}
			}
		}
		if (counter >= 2)
			return false;
		else
			return true;
	}

	static boolean isThereThereMoreThan1TheorySessionInTheSameDay(int[] vector, int slotHead, int dayInNumber,
			CourseInfo courseInSlotHead, ArrayList<CourseInfo> courseInfo) {

		boolean areStudentshaveMoreThan1TheorySessionInTheSameDay = false;
		for (int i = dayInNumber * 63; i < dayInNumber * 63 + 63; i = i + 3) {
			CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo, vector[i]);
			if (vector[i] != -1) {
				if (courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
						&& courseInSlotHead.getAssignedSection().equals(courseInSlotHeadInLoop.getAssignedSection())) {
					areStudentshaveMoreThan1TheorySessionInTheSameDay = true;
				}
			}
		}
		return !areStudentshaveMoreThan1TheorySessionInTheSameDay;
	}

	static boolean areStdentFreeInATheoryTimeSlot(int[] vector, int slotHead, int dayInNumber,
			CourseInfo courseInSlotHead, ArrayList<CourseInfo> courseInfo, ArrayList<SlotInfo> slotInfo) {

		// this is for tracking any theory course is in the same time slot that
		// conflicts with the lab that we want to assigned
		boolean areStudentsFree = false;
		SlotInfo slotHeadInfo = SlotInfo.searchSlotInfoArryList(slotInfo, slotHead + "");
		for (int i = dayInNumber * 63; i < dayInNumber * 63 + 63 && !areStudentsFree; i = i + 3) {
			int slotHeadInLoop = i;
			SlotInfo slotHeadInLoopInfo = SlotInfo.searchSlotInfoArryList(slotInfo, slotHeadInLoop + "");
			if (vector[slotHeadInLoop] != -1) {
				if (slotHeadInfo.getDay().equals(slotHeadInLoopInfo.getDay())
						&& slotHeadInfo.getStartTime().equals(slotHeadInLoopInfo.getStartTime())) {
					CourseInfo courseInSlotHeadInLoop = CourseInfo.srachCourseInfoArryList(courseInfo,
							vector[slotHeadInLoop]);
					if (courseInSlotHead.getCourseYearSemester().equals(courseInSlotHeadInLoop.getCourseYearSemester())
							&& courseInSlotHead.getAssignedSection()
									.equals(courseInSlotHeadInLoop.getAssignedSection())) {
						areStudentsFree = true;
					}

				}
			}

		}

		return !areStudentsFree;
	}

	static void readTeacherAssignedCourseInfoFromFile() {
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

	static boolean searchATheoryCourseInVector(int[] vector, int courseId) {
		// search the vector for the course
		int i = 0;
		for (; i < numberOfTheorySlots; i++) {
			if (vector[i] == courseId) {
				break;
			}
		}
		if (i >= numberOfTheorySlots)
			return false;
		else
			return true;
	}

	static boolean searchALabCourseInVector(int[] vector, int courseId) {
		// search the vector for the course
		int i = numberOfTheorySlots;
		for (; i < numberOfTheorySlots + numberOfLabSlots; i++) {
			if (vector[i] == courseId) {
				break;
			}
		}
		if (i >= (numberOfTheorySlots + numberOfLabSlots))
			return false;
		else
			return true;
	}

	static int findACourseWithSameYearSemerterAndSectionAndNotInVector(int[] vector, int courseId,
			ArrayList<CourseInfo> courseInfo) {
		int randomCourseId;
		// search a course until found
		CourseInfo aCourse, randomCourse;

		Random rand = new Random();

		aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);

		do {
			// randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberofTheoryCourses);
			randomCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);
		} while (!aCourse.getCourseYearSemester().equals(randomCourse.getCourseYearSemester())
				|| !aCourse.getAssignedSection().equals(randomCourse.getAssignedSection())
				|| searchATheoryCourseInVector(vector, randomCourseId));

		return randomCourseId;
	}

	static int findACourseWithSameYearSemerterAndSectionAndNotInVector(int[] vector, int courseId, int currentSlotIndex,
			ArrayList<CourseInfo> courseInfo, ArrayList<SlotInfo> slotInfo) {
		int randomCourseId;
		// search a course until found
		CourseInfo aCourse, randomCourse;

		Random rand = new Random();

		aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);

		do {
			// randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberofTheoryCourses);
			randomCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);
		} while (!aCourse.getCourseYearSemester().equals(randomCourse.getCourseYearSemester())
				|| !aCourse.getAssignedSection().equals(randomCourse.getAssignedSection())
				|| searchATheoryCourseInVector(vector, randomCourseId)
				|| isTheTeacherFree(vector, randomCourse, currentSlotIndex, courseInfo, slotInfo));

		return randomCourseId;
	}

	static int findATheoryCourseWhichIsNotAppearInVector(int[] vector, int currentSlotIndex,
			ArrayList<CourseInfo> courseInfo, ArrayList<SlotInfo> slotInfo) {
		int randomCourseId;
		CourseInfo aCourse;
		// search a course until found
		Random rand = new Random();
		boolean b1, b2, b3;

		do {
			// randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberofTheoryCourses);
			aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);

			b1 = searchATheoryCourseInVector(vector, randomCourseId);
			b2 = aCourse.getCourseNo().equals("freetime");
			b3 = isTheTeacherFree(vector, aCourse, currentSlotIndex, courseInfo, slotInfo);
		} while (b1 || b2 || b3);

		return randomCourseId;

	}

	static int findATheoryCourseWhichIsNotAppearInVector(int[] vector, ArrayList<CourseInfo> courseInfo) {
		int randomCourseId;
		CourseInfo aCourse;
		// search a course until found
		Random rand = new Random();

		do {
			// randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberofTheoryCourses);
			aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);

		} while (searchATheoryCourseInVector(vector, randomCourseId) || aCourse.getCourseNo().equals("freetime"));

		return randomCourseId;

	}

	static int findALabCourseWhichIsNotAppearInVector(int[] vector, int currentSlotIndex,
			ArrayList<CourseInfo> courseInfo, ArrayList<SlotInfo> slotInfo) {
		int randomCourseId;
		CourseInfo aCourse;
		// search a course until found
		Random rand = new Random();

		do {
			// randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberOfLabSlots) + numberOfTheorySlots;
			aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);

		} while (searchALabCourseInVector(vector, randomCourseId) || aCourse.getCourseNo().equals("freetime")
				|| isTheTeacherFree(vector, aCourse, currentSlotIndex, courseInfo, slotInfo));

		return randomCourseId;

	}

	static int findALabCourseWhichIsNotAppearInVector(int[] vector, ArrayList<CourseInfo> courseInfo) {
		int randomCourseId;
		CourseInfo aCourse;
		// search a course until found
		Random rand = new Random();

		do {
			// randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberOfLabSlots) + numberOfTheorySlots;
			aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);

		} while (searchALabCourseInVector(vector, randomCourseId) || aCourse.getCourseNo().equals("freetime"));

		return randomCourseId;

	}

}

/**
 * Class representing a TSP (Traveling Salesman Problem) problem.
 */
public class AUSTCSERoutineMultiObjectiveProblemV2WithSC extends Problem {

	final int numberOfTheorySlots = 378;
	final int numberOfLabSlots = 144;
	final int numberofTheoryCourses = 300;

	final int totalnumberOfTheorySession = 126;
	final int numberOfTheorySession = 100;

	ArrayList<CourseInfo> courseInfo;
	ArrayList<SlotInfo> slotInfo;

	ModellingObjectivesV2 modellingObjectives;
	ModellingConstraints modellingConstraints;
	ModellingSoftConstraints modellingSoftConstraints;

	boolean searchATheoryCourseInVector(int[] vector, int courseId) {
		// search the vector for the course
		int i = 0;
		for (; i < numberOfTheorySlots; i++) {
			if (vector[i] == courseId) {
				break;
			}
		}
		if (i >= numberOfTheorySlots)
			return false;
		else
			return true;
	}

	boolean searchALabCourseInVector(int[] vector, int courseId) {
		// search the vector for the course
		int i = numberOfTheorySlots;
		for (; i < numberOfTheorySlots + numberOfLabSlots; i++) {
			if (vector[i] == courseId) {
				break;
			}
		}
		if (i >= (numberOfTheorySlots + numberOfLabSlots))
			return false;
		else
			return true;
	}

	void numberOfAvailablecourses(int[] vector, int courseId) {
		int randomCourseId;
		// search a course until found
		CourseInfo aCourse, randomCourse;

		// for tracking
		int counter = 0;
		aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
		for (int i = 0; i < numberOfTheorySlots; i++) {
			randomCourse = CourseInfo.srachCourseInfoArryList(courseInfo, i);
			if (aCourse.getCourseYearSemester().equals(randomCourse.getCourseYearSemester())
					&& aCourse.getAssignedSection().equals(randomCourse.getAssignedSection())
					&& !searchATheoryCourseInVector(vector, i)) {
				counter++;
			}
		}
		System.out.println("total available course: " + counter);
	}

	int findATheoryCourseWhichIsNotAppearInVector(int[] vector, SlotInfo slot) {
		int randomCourseId;
		CourseInfo aCourse;
		// search a course until found
		Random rand = new Random();

		do {
			// randomCourseId = PseudoRandom.randInt(0, numberOfTheorySlots-1);
			randomCourseId = rand.nextInt(numberofTheoryCourses);
			aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, randomCourseId);

		} while (searchATheoryCourseInVector(vector, randomCourseId) || aCourse.getCourseNo().equals("freetime"));

		return randomCourseId;

	}

	boolean isTheFreeSlotAssigned(int[] vector, int freeSlotId) {
		int i = 0;
		for (; i < vector.length; i++) {
			if (vector[i] == freeSlotId)
				break;
		}
		if (i >= vector.length)
			return false;
		else
			return true;
	}

	
	 //This creatvarible is work without teacher constraints 
	public Solution createVariable() {
	  
		int counter = 0;
		Random rand = new Random();
		int [] vector_=new int[numberOfTheorySlots+numberOfLabSlots]; 
		for(int i=0;i<vector_.length;i++) 
			vector_[i]=-1;
		for(int i=0;i<100;i++) { 
			//find a random slot which is not occupied 
			int randomDay; // 0 to 5 
			int randomSlot; // 0 to 21 (in a day) 
			int slotHead;
	 
			//find a course that is not appeared in the vector 
			int courseId; 
			CourseInfo aCourse;
			
			courseId = SupportForCreatVariables.findATheoryCourseWhichIsNotAppearInVector(vector_, courseInfo); 
			aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
	 
			do { 
				
				randomSlot = rand.nextInt( 21); 
				randomDay = rand.nextInt(6); 
				slotHead = randomSlot*3+randomDay*63;
	 
			}while(vector_[slotHead]!=-1 
					|| !SupportForCreatVariables.areStdentFreeInATheoryTimeSlot(vector_, slotHead, randomDay, aCourse, courseInfo, slotInfo) 
					|| !SupportForCreatVariables.isThereThereMoreThan1TheorySessionInTheSameDay(vector_, slotHead, randomDay, aCourse, courseInfo) 
					|| !SupportForCreatVariables.isThereMoreThan2LabsInTheSameDay(vector_, slotHead, randomDay, aCourse, courseInfo) 
					|| !SupportForCreatVariables.isThereMoreThan3TheoryAnd1LabInTheSameDay(vector_, slotHead, randomDay, aCourse, courseInfo) );
	
	 /* vector_[slotHead+0]=SupportForCreatVariables.
	 * findATheoryCourseWhichIsNotAppearInVector(vector_, slotHead, courseInfo,
	 * slotInfo); vector_[slotHead+1]=SupportForCreatVariables.
	 * findATheoryCourseWhichIsNotAppearInVector(vector_, slotHead+1, courseInfo,
	 * slotInfo); vector_[slotHead+2]=SupportForCreatVariables.
	 * findATheoryCourseWhichIsNotAppearInVector(vector_, slotHead+2, courseInfo,
	 * slotInfo);*/
	 
	 		vector_[slotHead+0]=courseId; 	
	 		vector_[slotHead+1]=SupportForCreatVariables.findACourseWithSameYearSemerterAndSectionAndNotInVector(vector_, courseId, courseInfo); 
	 		vector_[slotHead+2]=SupportForCreatVariables.findACourseWithSameYearSemerterAndSectionAndNotInVector(vector_, courseId, courseInfo); 
	  
	  }
	  
	  //assign theory freeslots randomly 
		for(int i=0;i<numberOfTheorySlots;i++) {
	  
			if(vector_[i]==-1) { 
				int randomFreeSlot; 
				do{
					randomFreeSlot = rand.nextInt((numberOfTheorySlots - numberofTheoryCourses)) + numberofTheoryCourses; 
				}while(isTheFreeSlotAssigned(vector_, randomFreeSlot));

				vector_[i]= randomFreeSlot; 
			} 
		}
		
	 
	  //assign labs randomly while not violating constraint int randomLabSlot;
	 for(int i=0;i<97;i++) {
	  
		 int randomDay; // 0 to 5 
		 int randomSlot; // 0 to 21 (in a day) 
		 int slotHead;
	 
		 int courseId; 
		 CourseInfo aCourse;
	 
		 courseId=SupportForCreatVariables.findALabCourseWhichIsNotAppearInVector(vector_, courseInfo); 
		 aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);
		 
	 
		 do { 
	 
 			randomSlot = rand.nextInt(24); 
 			randomDay = rand.nextInt(6); 
 			slotHead = randomSlot+randomDay*24+numberOfTheorySlots;
		 }while(vector_[slotHead]!=-1 
				 || !SupportForCreatVariables.areStdentFreeInALabTimeSlot(vector_, slotHead, randomDay, aCourse, courseInfo, slotInfo) 
				 || !SupportForCreatVariables.isThereMoreThan2LabsInTheSameDay(vector_, slotHead, randomDay, aCourse, courseInfo) 
				 || !SupportForCreatVariables.isThereMoreThan3TheoryAnd1LabInTheSameDay(vector_, slotHead, randomDay, aCourse, courseInfo) );
			 
		 vector_[slotHead]=courseId;
	 
	 }
	  
	 //assign lab freeslots randomly (the lab free slots starts from index 475
	 for(int i=numberOfTheorySlots;i<numberOfTheorySlots+numberOfLabSlots;i++) {
		 if(vector_[i]==-1) { 
			 int randomFreeSlot;
			 do{ 
				 randomFreeSlot = rand.nextInt(numberOfTheorySlots+numberOfLabSlots - 475) + 475 ; 
				 }while(isTheFreeSlotAssigned(vector_, randomFreeSlot));
	 
			 vector_[i]= randomFreeSlot;
		 }	 
	 }
	  
	  
	 Solution s=null; 
	 try{
		 	s = new Solution(this); 
		}catch (ClassNotFoundException e) { 
			 e.printStackTrace(); 
		}
	 
	 ((Permutation)s.getDecisionVariables()[0]).vector_=vector_;
	
	 return s; 
	}
	 

	/*public Solution createVariable() {

		int counter = 0;

		Random rand = new Random();

		int[] vector_ = new int[numberOfTheorySlots + numberOfLabSlots];
		for (int i = 0; i < vector_.length; i++)
			vector_[i] = -1;

		for (int i = 0; i < 100; i++) {
			// find a random slot which is not occupied
			int randomDay; // 0 to 5
			int randomSlot; // 0 to 21 (in a day)
			int slotHead;

			// find a course that is not appeared in the vector
			int courseId;
			CourseInfo aCourse;

			do {

				randomSlot = rand.nextInt(21);
				randomDay = rand.nextInt(6);
				slotHead = randomSlot * 3 + randomDay * 63;

				System.out.println("line 742");
				courseId = SupportForCreatVariables.findATheoryCourseWhichIsNotAppearInVector(vector_, slotHead,
						courseInfo, slotInfo);

				aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);

			} while (vector_[slotHead] != -1);
					/*|| !SupportForCreatVariables.areStdentFreeInATheoryTimeSlot(vector_, slotHead, randomDay, aCourse,
							courseInfo, slotInfo)
					|| !SupportForCreatVariables.isThereThereMoreThan1TheorySessionInTheSameDay(vector_, slotHead,
							randomDay, aCourse, courseInfo)
					|| !SupportForCreatVariables.isThereMoreThan2LabsInTheSameDay(vector_, slotHead, randomDay, aCourse,
							courseInfo)
					|| !SupportForCreatVariables.isThereMoreThan3TheoryAnd1LabInTheSameDay(vector_, slotHead, randomDay,
							aCourse, courseInfo));*/

		/*	vector_[slotHead + 0] = courseId;
			System.out.println("line 751");
			vector_[slotHead + 1] = SupportForCreatVariables.findACourseWithSameYearSemerterAndSectionAndNotInVector(
					vector_, courseId, slotHead + 1, courseInfo, slotInfo);

			System.out.println("line 752");
			vector_[slotHead + 2] = SupportForCreatVariables.findACourseWithSameYearSemerterAndSectionAndNotInVector(
					vector_, courseId, slotHead + 2, courseInfo, slotInfo);

			System.out.println("line 753");

		}

		// assign theory freeslots randomly
		for (int i = 0; i < numberOfTheorySlots; i++) {

			// System.out.println(i);
			if (vector_[i] == -1) {
				int randomFreeSlot;
				do {
					randomFreeSlot = rand.nextInt((numberOfTheorySlots - numberofTheoryCourses))
							+ numberofTheoryCourses;
				} while (isTheFreeSlotAssigned(vector_, randomFreeSlot));

				vector_[i] = randomFreeSlot;
			}
		}

		// assign labs randomly while not violating constraint
		int randomLabSlot;
		for (int i = 0; i < 97; i++) {

			int randomDay; // 0 to 5
			int randomSlot; // 0 to 21 (in a day)
			int slotHead;

			int courseId;
			CourseInfo aCourse;

			do {
			
				randomSlot = rand.nextInt(24);
				randomDay = rand.nextInt(6);
				slotHead = randomSlot + randomDay * 24 + numberOfTheorySlots;

				System.out.println("Line 771");
				courseId = SupportForCreatVariables.findALabCourseWhichIsNotAppearInVector(vector_, slotHead,
						courseInfo, slotInfo);
				aCourse = CourseInfo.srachCourseInfoArryList(courseInfo, courseId);

			} while (vector_[slotHead] != -1
					|| !SupportForCreatVariables.areStdentFreeInALabTimeSlot(vector_, slotHead, randomDay, aCourse,
							courseInfo, slotInfo)
					|| !SupportForCreatVariables.isThereMoreThan2LabsInTheSameDay(vector_, slotHead, randomDay, aCourse,
							courseInfo)
					|| !SupportForCreatVariables.isThereMoreThan3TheoryAnd1LabInTheSameDay(vector_, slotHead, randomDay,
							aCourse, courseInfo));

			
			vector_[slotHead] = courseId;

		}

		// assign lab freeslots randomly (the lab free slots starts from index 475
		for (int i = numberOfTheorySlots; i < numberOfTheorySlots + numberOfLabSlots; i++) {
			if (vector_[i] == -1) {
				int randomFreeSlot;

				do {
					randomFreeSlot = rand.nextInt(numberOfTheorySlots + numberOfLabSlots - 475) + 475;
				} while (isTheFreeSlotAssigned(vector_, randomFreeSlot));

				vector_[i] = randomFreeSlot;

			}
		}


		System.out.println("line 802");
		Solution s = null;
		try {

			s = new Solution(this);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		((Permutation) s.getDecisionVariables()[0]).vector_ = vector_;

		return s;
	}
		 */
	/**
	 * Creates a new TSP problem instance. It accepts data files from TSPLIB
	 * 
	 * @param filename
	 *            The file containing the definition of the problem
	 */
	public AUSTCSERoutineMultiObjectiveProblemV2WithSC(String solutionType) {

		numberOfVariables_ = 1;
		numberOfObjectives_ = 2;
		numberOfConstraints_ = 6;
		numberOfSoftConstraints_ = 1;
		problemName_ = "AUSTCSERoutineProblem";
		solutionType_ = new PermutationSolutionType(this);

		length_ = new int[numberOfVariables_];

		// storing teacher course info
		ArrayList<TeacherAssignedCourseInfo> teacherAssignedCourseInfo;

		// modelling objective
		modellingObjectives = new ModellingObjectivesV2();

		// modeliing constraints
		modellingConstraints = new ModellingConstraints();

		// modeliing soft constraints
		modellingSoftConstraints = new ModellingSoftConstraints();

		// read courseInfo from courInfo.csv file
		readCourseInfoFromFile();

		// read slotInfo from slotInfo.csv file
		readSlotInfoFromFile();

		// read teacher assigned course
		SupportForCreatVariables.readTeacherAssignedCourseInfoFromFile();

		try {
			if (solutionType.compareTo("Permutation") == 0)
				solutionType_ = new PermutationSolutionType(this);
			else {
				throw new JMException("Solution type invalid");
			}
		} catch (JMException e) {
			e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
		}
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

	public void fillClassroomTimeslotTable(Solution solution) {

		SlotInfo slotInfo;
		CourseInfo courseInfo;

		int length = ((Permutation) solution.getDecisionVariables()[0]).vector_.length;

		for (int i = 0; i < length; i++) {

			slotInfo = SlotInfo.searchSlotInfoArryList(this.slotInfo, i + "");
			courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo,
					((Permutation) solution.getDecisionVariables()[0]).vector_[i]);

			modellingObjectives.fillUpTheMap(slotInfo, courseInfo);
			modellingConstraints.updateAllMaps(slotInfo, courseInfo);
			modellingSoftConstraints.updateAllMaps(slotInfo, courseInfo);

		}

	}

	/**
	 * Evaluates a solution
	 * 
	 * @param solution
	 *            The solution to evaluate
	 */
	public void evaluate(Solution solution) {

		fillClassroomTimeslotTable(solution);

		double obj1 = modellingObjectives.calculateTotalStudentsTime();

		double obj2 = modellingObjectives.calculateTotalTeachertime();

		solution.setObjective(0, obj1);
		solution.setObjective(1, -1 * obj2);

		modellingObjectives.clearAllMaps();
	} // evaluate

	public void evaluateConstraints(Solution solution) throws JMException {
		double totalConstraints = 0;
		int numberOfConstraints = 0;

		double calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab = modellingConstraints
				.calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab();
		if (calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab > 0.0) {
			totalConstraints += (-1 * calculateConstraintsNotMoreThanThreeTheoryClassAndOneLab);
			numberOfConstraints++;
		}

		double calculateConstraintsForNotMoreThanTwoLabsInADayForStudents = modellingConstraints
				.calculateConstraintsNotMoreThanTwoLabClassAndOneLab();
		if (calculateConstraintsForNotMoreThanTwoLabsInADayForStudents > 0) {
			totalConstraints += (-1 * calculateConstraintsForNotMoreThanTwoLabsInADayForStudents);
			numberOfConstraints++;
		}

		double calculateConstraintsNotMoreThanFourTheoryClassAndOneLab = modellingConstraints
				.calculateConstraintsNotMoreThanFourTheoryClassAndOneLab();
		if (calculateConstraintsNotMoreThanFourTheoryClassAndOneLab > 0) {
			totalConstraints += (-1 * calculateConstraintsNotMoreThanFourTheoryClassAndOneLab);
			numberOfConstraints++;
		}

		double calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay = modellingConstraints
				.calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay();
		if (calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay > 0) {
			totalConstraints += (-1
					* calculatingConstraintsForAllTheoryClassesMustBeExecuteInTheSameRoomInASessionInADay);
			numberOfConstraints++;
		}

		double calculatingConstraintsOfSameTimeClassForATeacher = modellingConstraints
				.calculatingConstraintsOfSameTimeClassForATeacher();
		if (calculatingConstraintsOfSameTimeClassForATeacher > 0) {
			totalConstraints += (-1 * calculatingConstraintsOfSameTimeClassForATeacher);
			numberOfConstraints++;
		}

		double calculatingConstraintsOfSameTimeClassForAStudent = modellingConstraints
				.calculatingConstraintsOfSameTimeClassForAStudent();
		if (calculatingConstraintsOfSameTimeClassForAStudent > 0) {
			totalConstraints += (-1 * calculatingConstraintsOfSameTimeClassForAStudent);
			numberOfConstraints++;
		}

		solution.setOverallConstraintViolation(totalConstraints);
		solution.setNumberOfViolatedConstraint(numberOfConstraints);

		modellingConstraints.clearAllMaps();
	}

	public void evaluateSoftConstraints(Solution solution) throws JMException {
		double totalConstraints = 0;
		int numberOfConstraints = 0;
		double calculateConstraintsOfTeacherPreferredSlots = modellingSoftConstraints
				.calculateConstraintsOfTeacherPreferredSlots();
		if (calculateConstraintsOfTeacherPreferredSlots > 0) {
			totalConstraints += (-1 * calculateConstraintsOfTeacherPreferredSlots);
			numberOfConstraints++;
		}
		solution.setOverallSoftConstraintViolation(totalConstraints);
		solution.setNumberOfViolatedSoftConstraint(numberOfConstraints);

		modellingSoftConstraints.clearAllMaps();
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

		// long initTime = System.currentTimeMillis();

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

		// long estimatedTime = System.currentTimeMillis() - initTime;
		// System.out.println("Time (repair): "+estimatedTime);

		// checking(s);
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
