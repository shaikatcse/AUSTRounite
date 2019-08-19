package aust.cse.routine.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SlotInfo {
	String slotNumericId,  roomNo, startTime, endTime, day, slotID, slotType, session;

	
	
	public String getSlotType() {
		return slotType;
	}

	public void setSlotType(String slotType) {
		this.slotType = slotType;
	}
	
	

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public SlotInfo(String slotNumericId, String roomNo, String startTime, String endTime, String day, String slotID,
			String slotType, String session) {
		super();
		this.slotNumericId = slotNumericId;
		this.roomNo = roomNo;
		this.startTime = startTime;
		this.endTime = endTime;
		this.day = day;
		this.slotID = slotID;
		this.slotType = slotType;
		this.session = session;
	}

	public String getSlotNumericId() {
		return slotNumericId;
	}

	public void setSlotNumericId(String slotNumericId) {
		this.slotNumericId = slotNumericId;
	}

	public String getRoomNo() {
		return roomNo;
	}

	public void setRoomNo(String roomNo) {
		this.roomNo = roomNo;
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

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getSlotID() {
		return slotID;
	}

	public void setSlotID(String slotID) {
		this.slotID = slotID;
	}

	
	
	public static SlotInfo searchSlotInfoArryList(List<SlotInfo> slotInfo, String slotNumericId){
		for(SlotInfo o: slotInfo){
			if( o.getSlotNumericId().equals(slotNumericId))
				return o; 
		}
		return null;
		
	}
	
	public static SlotInfo searchSlotInfoArryListBySlotID(List<SlotInfo> slotInfo, String slotID){
		for(SlotInfo o: slotInfo){
			if( o.getSlotID().equals(slotID))
				return o; 
		}
		return null;
		
	}
	
	

	
public static void main(String args[]) throws IOException{
		
		BufferedReader br = null;
	    String line = "";
	    String csvFile = "SlotInfo.csv";
	    String cvsSplitBy = ",";
	    
		ArrayList<SlotInfo> slotInfo = new ArrayList<SlotInfo>();
		br = new BufferedReader(new FileReader(csvFile));
		line = br.readLine(); 
		while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] info = line.split(cvsSplitBy);

            SlotInfo infoSlot = new SlotInfo(
            		info[0], info[1], info[2], info[3], info[4], info[5], info[6] );

            slotInfo.add(infoSlot);
            
        }
		
		for(int i=0;i<slotInfo.size();i++){
			SlotInfo infoSlot = slotInfo.get(i);
			System.out.println(infoSlot.getSlotNumericId()+" "+infoSlot.getSlotID());
		}
	
	
		SlotInfo si=srachSlotInfoArryList(slotInfo, "0");
	
		System.out.println();
		System.out.println(si.getSlotID());
	}

}

