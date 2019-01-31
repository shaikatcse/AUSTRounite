package aust.cse.routine.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CourseInfo {
	int id;
	String courseNo, assignedSection, assignedLabSection, courseType, courseYearSemester;
	
	public String getCourseYearSemester() {
		return courseYearSemester;
	}
	public void setCourseYearSemester(String courseYearSemester) {
		this.courseYearSemester = courseYearSemester;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCourseNo() {
		return courseNo;
	}
	public void setCourseNo(String courseNo) {
		this.courseNo = courseNo;
	}
	public String getAssignedSection() {
		return assignedSection;
	}
	public void setAssignedSection(String assignedSection) {
		this.assignedSection = assignedSection;
	}
	public String getAssignedLabSection() {
		return assignedLabSection;
	}
	public void setAssignedLabSection(String assignedLabSection) {
		this.assignedLabSection = assignedLabSection;
	}
	public String getCourseType() {
		return courseType;
	}
	public void setCourseType(String courseType) {
		this.courseType = courseType;
	}
	public CourseInfo(int id, String courseNo, String assignedSection, String assignedLabSection, String courseType, String courseYearSemester) {
		super();
		this.id = id;
		this.courseNo = courseNo;
		this.assignedSection = assignedSection;
		this.assignedLabSection = assignedLabSection;
		this.courseType = courseType;
		this.courseYearSemester = courseYearSemester;
	}
	
	
	public static CourseInfo srachCourseInfoArryList(List<CourseInfo> courseInfo, int courseId){
		for(CourseInfo o: courseInfo){
			if( o.getId() == courseId)
				return o; 
		}
		return null;
		
	}
	
	
	public static void main(String args[]) throws IOException{
		
		BufferedReader br = null;
	    String line = "";
	    String csvFile = "Map01.csv";
	    String cvsSplitBy = ",";
	    
		ArrayList<CourseInfo> classInfo = new ArrayList<CourseInfo>();
		br = new BufferedReader(new FileReader(csvFile));
		line = br.readLine(); 
		while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] info = line.split(cvsSplitBy);

            CourseInfo infoClass = new CourseInfo(
            		Integer.parseInt(info[0]), info[1], info[2], info[3], info[4], info[5] );

            classInfo.add(infoClass);
            
        }
		
		for(int i=0;i<classInfo.size();i++){
			CourseInfo infoClass = classInfo.get(i);
			System.out.println(infoClass.getId()+" "+ infoClass.getCourseNo());
		}
	
	}

}

