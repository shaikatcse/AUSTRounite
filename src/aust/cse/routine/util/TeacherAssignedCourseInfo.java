package aust.cse.routine.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TeacherAssignedCourseInfo {
	String teacherName,  assignedCourseNo, assignedCourseSection, assignedCourseStudentGroup;

	
	
public TeacherAssignedCourseInfo(String teacherName, String assignedCourseNo, String assignedCourseSection,
			String assignedCourseStudentGroup) {
		super();
		this.teacherName = teacherName;
		this.assignedCourseNo = assignedCourseNo;
		this.assignedCourseSection = assignedCourseSection;
		this.assignedCourseStudentGroup = assignedCourseStudentGroup;
	}



public String getTeacherName() {
		return teacherName;
	}



	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}



	public String getAssignedCourseNo() {
		return assignedCourseNo;
	}



	public void setAssignedCourseNo(String assignedCourseNo) {
		this.assignedCourseNo = assignedCourseNo;
	}



	public String getAssignedCourseSection() {
		return assignedCourseSection;
	}



	public void setAssignedCourseSection(String assignedCourseSection) {
		this.assignedCourseSection = assignedCourseSection;
	}



	public String getAssignedCourseStudentGroup() {
		return assignedCourseStudentGroup;
	}



	public void setAssignedCourseStudentGroup(String assignedCourseStudentGroup) {
		this.assignedCourseStudentGroup = assignedCourseStudentGroup;
	
	}


	public String toString() {
		return getTeacherName()+" "+getAssignedCourseNo()+" "+getAssignedCourseSection()+" "+getAssignedCourseStudentGroup();
	}


public static void main(String args[]) throws IOException{
		
		BufferedReader br = null;
	    String line = "";
	    String csvFile = "TeacherAssingedCourseInfo.csv";
	    String cvsSplitBy = ",";
	    
		ArrayList<TeacherAssignedCourseInfo> teacherAssignedCourseInfo = new ArrayList<TeacherAssignedCourseInfo>();
		br = new BufferedReader(new FileReader(csvFile));
		line = br.readLine(); 
		while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] info = line.split(cvsSplitBy);

            TeacherAssignedCourseInfo infoSlot = new TeacherAssignedCourseInfo(
            		info[0], info[1], info[2], info[3] );

            teacherAssignedCourseInfo.add(infoSlot);
            
        }
		
		for(int i=0;i<teacherAssignedCourseInfo.size();i++){
			
			System.out.println(teacherAssignedCourseInfo.get(i));
		}
	
	
	
	}

}

