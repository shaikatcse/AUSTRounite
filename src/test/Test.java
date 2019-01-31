package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Test {

	static Connection conn;
	public static double calculateObjective1() {
		double obj1 = 0.0;
	
		
		String query="SELECT DISTINCT Course.ysID, Timeslot.Day, MIN(time(Timeslot.startTime)) as Starting_Time, MAX(time(Timeslot.endTime)) as Finishing_Time, CourseClassroomTimeslot.Section, CourseClassroomTimeslot.StudentGroup, ((MAX(strftime('%s', Timeslot.endTime)) - MIN(strftime('%s', Timeslot.startTime)))/3600.0) as duration, count(Timeslot.Day) from Timeslot, CourseClassroomTimeslot, Course where Timeslot.slotID = CourseClassroomTimeslot.slotID and CourseClassroomTimeslot.CourseNo = Course.CourseNo "+
    			"GROUP BY Timeslot.DAY,CourseClassroomTimeslot.Section,Course.ysID, CourseClassroomTimeslot.StudentGroup "+
    			"ORDER BY Course.ysID ASC, CourseClassroomTimeslot.section ASC, case when Day= 'Saturday' then 1 when Day= 'Sunday' then 2 when Day= 'Monday' then 3 "+
    			              "when Day= 'Tuesday' then 4 when Day= 'Wednesday' then 5 "+ 
    			              "else 6 end asc, time(startTime) ASC";
		
		try{
			PreparedStatement statement = conn.prepareStatement(query);
    	
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				//System.out.println("duration" + rs.getString("duration") );
				obj1 += Double.parseDouble(rs.getString("duration"));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	return obj1;
	}	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		try{
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\shaik\\Desktop\\TimeTableTestDB3_backup.sqlite");
           
        }catch(Exception e){
            e.printStackTrace();
                   }
	
		for(int i=0;i<50000;i++) {
			System.out.println(i);
			if(i%10000==0) {
				conn.close();
			    conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\shaik\\Desktop\\TimeTableTestDB3_backup.sqlite");
			     			
			}
			calculateObjective1();
		}
	}
	
	

}
