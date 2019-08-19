package aust.cse.routine.analyseResults;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingConstraints;
import aust.cse.routine.problem.multiObjective.withoutDataBase.ModellingObjectivesV2;
import aust.cse.routine.util.CourseInfo;
import aust.cse.routine.util.SlotInfo;
import jmetal.core.Solution;
import jmetal.encodings.variable.Permutation;

class RoutineRow {
	String courseNo, slotID, roomNo, section, studentGroup, session;

	public String getCourseNo() {
		return courseNo;
	}

	public void setCourseNo(String courseNo) {
		this.courseNo = courseNo;
	}

	public String getSlotID() {
		return slotID;
	}

	public void setSlotID(String slotID) {
		this.slotID = slotID;
	}

	public String getRoomNo() {
		return roomNo;
	}

	public void setRoomNo(String roomNo) {
		this.roomNo = roomNo;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getStudentGroup() {
		return studentGroup;
	}

	public void setStudentGroup(String studentGroup) {
		this.studentGroup = studentGroup;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public RoutineRow(String courseNo, String slotID, String roomNo, String section, String studentGroup,
			String session) {
		super();
		this.courseNo = courseNo;
		this.slotID = slotID;
		this.roomNo = roomNo;
		this.section = section;
		this.studentGroup = studentGroup;
		this.session = session;
	}

}

public class CalculateManualRoutineObjectives {

	ArrayList<CourseInfo> courseInfo;
	ArrayList<SlotInfo> slotInfo;
	ArrayList<RoutineRow> routine;

	ModellingObjectivesV2 modellingObjectives;
	ModellingConstraints modellingConstraints;

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

	void readRoutineFromFile() {
		BufferedReader br = null;
		String line = "";
		String csvFile = "routine.csv";
		String cvsSplitBy = ",";

		routine = new ArrayList<RoutineRow>();
		try {
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);

				RoutineRow routineRow = new RoutineRow(info[0], info[1], info[2], info[3], info[4], info[5]);

				routine.add(routineRow);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void fillClassroomTimeslotTable(ArrayList<RoutineRow> routine) {

		SlotInfo slotInfo;
		CourseInfo courseInfo;

		RoutineRow routineRow;

		int length = routine.size();

		for (int i = 0; i < length; i++) {

			routineRow = routine.get(i);
			slotInfo = SlotInfo.searchSlotInfoArryListBySlotID(this.slotInfo, routineRow.getSlotID());
			courseInfo = CourseInfo.srachCourseInfoArryList(this.courseInfo, routineRow.getCourseNo(),
					routineRow.getSection(), routineRow.getStudentGroup());

			modellingObjectives.fillUpTheMap(slotInfo, courseInfo);

		}

	}

	void calculateObjectives(){
		readSlotInfoFromFile();
		readCourseInfoFromFile();
		readRoutineFromFile();
		modellingObjectives = new ModellingObjectivesV2();
		fillClassroomTimeslotTable(routine);
		double obj1 = modellingObjectives.calculateTotalStudentsTime();

		double obj2 = modellingObjectives.calculateTotalTeachertime();

		System.out.println(obj1+ " "+obj2);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CalculateManualRoutineObjectives c = new CalculateManualRoutineObjectives();
		c.calculateObjectives();
	
	}

}
