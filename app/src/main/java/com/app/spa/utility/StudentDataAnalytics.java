package com.app.spa.utility;

import com.app.spa.service.repository.Student;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class StudentDataAnalytics {
    HashMap<String, Integer> mLevelToStudentsCountActualMap = new HashMap<>();
    Map<String,Student> studentMap;
    public StudentDataAnalytics(Map<String, Student> studentMap){
        this.studentMap = studentMap;

    }

    public static int getCurrMonthActiveStudentCount(Map<String,Student> stuMap){
        int currPresentCnt=0;
        for (Map.Entry<String,Student> e: stuMap.entrySet()) {
            Student st = e.getValue();
            if(st.isPresentCurrentMnth())
                currPresentCnt++;
        }
        return currPresentCnt;
    }

    public String calcLevelwiseStudentCount(){

        for (Object st: studentMap.keySet()) {
            Student temStudent = studentMap.get(st);
            String level = temStudent.Level==null || "".equals(temStudent.Level)? "NO_LEVEL":temStudent.Level;
            Integer levelCnt = mLevelToStudentsCountActualMap.get(level)!=null?mLevelToStudentsCountActualMap.get(level):0;
            mLevelToStudentsCountActualMap.put(level.trim(),++levelCnt);
        }

        StringBuffer summaryText = new StringBuffer("<html>" +
                "<style>" +
                "table, td, th {" +
                "  border: 1px solid black;" +
                "  font-family:sans-serif-medium " +
                "} " +
                "table {" +
                "  border-collapse: collapse;" +
                "  width: 100%;" +
                "  font-family:sans-serif-medium" +
                "}" +
                "th {" +
                "  height: 70px;" +
                " font-family:sans-serif-medium" +
                "}" +
                "</style>" +
                "</head><body>" +
            " <table>" +
            "<tr><th colspan=\"2\">Level Wise Student Report </th></tr>");
        for (String tLevel : mLevelToStudentsCountActualMap.keySet()) {
            Integer tCount = mLevelToStudentsCountActualMap.get(tLevel);
            summaryText.append("<tr><td>"+tLevel +"</td><td> "+ tCount + "</td></tr>");//
        }
        summaryText.append("</table></body></html>");
        return summaryText.toString();
    }
}
