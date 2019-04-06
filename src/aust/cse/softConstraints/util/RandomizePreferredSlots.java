package aust.cse.softConstraints.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

class SlotInfo{
	String day, startTime, endTime;

	public SlotInfo(String day, String startTime, String endTime) {
		super();
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	

}

public class RandomizePreferredSlots {

	public static void main(String[] args) {
		ArrayList<String> teacherNames = new ArrayList<String>();
		ArrayList<SlotInfo> slotInfos = new ArrayList<>();

		
		BufferedReader br = null;
		String line = "";
		String csvFile = "TeacherPreferredSlot.csv";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);

				// 0 -> name
				// 1 -> day
				// 2 -> startTime
				// 3 -> endTime
				
				teacherNames.add(info[0]);
				slotInfos.add(new SlotInfo(info[1], info[2], info[3]));
				
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		Collections.shuffle(slotInfos);
		
		int i=0;
		for(String teacherName:teacherNames) {
			System.out.print(teacherName+",");
			System.out.println(slotInfos.get(i).getDay()+","+slotInfos.get(i).getStartTime()+","+slotInfos.get(i).getEndTime() );
			i++;
		}
		
	}
}
