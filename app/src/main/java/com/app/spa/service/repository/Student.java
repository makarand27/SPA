package com.app.spa.service.repository;


import android.text.TextUtils;

import com.app.spa.service.firestore.DBConstants;
import com.app.spa.service.firestore.StudentDao;
import com.google.common.base.Strings;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import lombok.Data;

@Data
public class Student{

    public String First_name;
    public String Last_name;
    public String Contact;
    public String Bday;
    public String Batch_name;
    public String Level;
    public String Fee_status;
    public String Is_Half_paid;
    public String Custom_fee;
    public String Joining_fee;
    public String Id;
    public boolean Joining_fee_paid;
    public String Joining_fee_paid_dt;

    public String Bank_ref;
    public String mSkills;
    public Date Last_attended;
    public int Monthly_attendance_count;
    public String Fee_Rcvd_dt;
    public String Joining_dt;
    public String Attendance_Summary;

    public volatile boolean fee_overdue;


    public boolean isAdmin = false;

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Student(){}

    public Student(StudentDao vDao){
        First_name = vDao.First_name == null ? "" : vDao.First_name.trim();
        Last_name = vDao.Last_name == null ? "" : vDao.Last_name.trim();
        Contact = vDao.Contact == null ? "" : vDao.Contact.trim();
        Bday = vDao.Bday == null ? "" : vDao.Bday.trim();
        Batch_name = vDao.Batch_name == null ? "" : vDao.Batch_name.trim();
        Level = vDao.Level == null ? "" : vDao.Level;
        Fee_status = vDao.Fee_status == null ? "" : vDao.Fee_status;
        Is_Half_paid = vDao.Is_Half_paid == null ? "" : vDao.Is_Half_paid;
        Custom_fee = vDao.Custom_fee == null ? "" : vDao.Custom_fee;
        Joining_fee = vDao.Joining_fee == null ? "" : vDao.Joining_fee;
        Joining_fee_paid = vDao.Joining_fee_paid;
        Joining_fee_paid_dt =  vDao.Joining_fee_paid_dt;
        Bank_ref = vDao.Bank_ref== null ? "" : vDao.Bank_ref.trim();
        mSkills = vDao.skills == null ? "" : vDao.skills.trim();
        Last_attended = vDao.Last_attended == null ? null : vDao.Last_attended;
        Monthly_attendance_count = vDao.Monthly_attendance_count == 0 ? 0 : vDao.Monthly_attendance_count;
        Fee_Rcvd_dt = vDao.Fee_Rcvd_dt == null ? "" : vDao.Fee_Rcvd_dt;
        Joining_dt = vDao.Joining_dt == null ? "" : vDao.Joining_dt;
        //Attendence_Summary = vDao.Attendence_Summary== "" ?null:vDao.Attendence_Summary;
    }

    public String getExportString(){

        String tJoining_fee_paid = Joining_fee_paid ?  "paid" : "pending";
        return First_name + ","
                + Last_name + ","
                + Batch_name + ","
                + Fee_status + ","
                + Contact + ","
                + Bday + ","
                + Level + ","
                + Custom_fee + ","
                + Joining_fee + ","
                + Joining_fee_paid_dt+","
                + Bank_ref + ","
                + mSkills + ","
                + Last_attended+","
                + Fee_Rcvd_dt
                + Joining_dt
                + Attendance_Summary;
    }
    public String getFullName(){
        return First_name + " " + Last_name;
    }

    public HashMap<String, Object> GetUpdateFields(Student vStudent){

        HashMap<String,Object> tUpdateData = new HashMap<>();
        //Compare
        if(!Last_name.equals(vStudent.Last_name)) tUpdateData.put(DBConstants.STUDENT.LAST_NAME,vStudent.Last_name.trim());
        if(!First_name.equals(vStudent.First_name)) tUpdateData.put(DBConstants.STUDENT.FIRST_NAME,vStudent.First_name.trim());
        if(!Batch_name.equals(vStudent.Batch_name)) tUpdateData.put(DBConstants.STUDENT.BATCH_NAME,vStudent.Batch_name.trim());
        if(!Fee_status.equals(vStudent.Fee_status)) tUpdateData.put(DBConstants.STUDENT.FEE_STATUS,vStudent.Fee_status);
        if(Is_Half_paid.equals(vStudent.Is_Half_paid)) tUpdateData.put(DBConstants.STUDENT.IS_HALF_PAID,vStudent.Is_Half_paid);
        if(!Contact.equals(vStudent.Contact)) tUpdateData.put(DBConstants.STUDENT.CONTACT,vStudent.Contact);
        if(!Bday.equals(vStudent.Bday)) tUpdateData.put(DBConstants.STUDENT.BDAY,vStudent.Bday);
        if(!Level.equals(vStudent.Level)) tUpdateData.put(DBConstants.STUDENT.LEVEL,vStudent.Level);
        if(!Custom_fee.equals(vStudent.Custom_fee)) tUpdateData.put(DBConstants.STUDENT.CUSTOM_FEE,vStudent.Custom_fee);
        if(!Joining_fee.equals(vStudent.Joining_fee)) tUpdateData.put(DBConstants.STUDENT.JOINING_FEE,vStudent.Joining_fee);
        if(Joining_fee_paid != vStudent.Joining_fee_paid) tUpdateData.put(DBConstants.STUDENT.JOINING_FEE_PAID,vStudent.Joining_fee_paid);

        if(!Joining_fee_paid && (Joining_fee_paid_dt != vStudent.Joining_fee_paid_dt)) tUpdateData.put(DBConstants.STUDENT.JOINING_FEE_PAID_DT,vStudent.Joining_fee_paid_dt);

        if(!Bank_ref.equals(vStudent.Bank_ref)) tUpdateData.put(DBConstants.STUDENT.BANK_REF,vStudent.Bank_ref.trim());
        if(!mSkills.equals(vStudent.mSkills)) tUpdateData.put(DBConstants.STUDENT.SKILLS,vStudent.mSkills.trim());
        if(vStudent.Last_attended !=null && Last_attended != vStudent.Last_attended ) tUpdateData.put(DBConstants.STUDENT.LAST_ATTENDED,vStudent.Last_attended);
        if(vStudent.Monthly_attendance_count !=0 && Monthly_attendance_count != vStudent.Monthly_attendance_count ) tUpdateData.put(DBConstants.STUDENT.MONTHLY_ATTENDANCE_COUNT,vStudent.Monthly_attendance_count);
        if(!Fee_Rcvd_dt.equals(vStudent.Fee_Rcvd_dt))tUpdateData.put(DBConstants.STUDENT.FEE_RCVD_DT,vStudent.Fee_Rcvd_dt);
        if(!Joining_dt.equals(vStudent.Joining_dt)) tUpdateData.put(DBConstants.STUDENT.JOINING_DT,vStudent.Joining_dt);
       // if(!Attendence_Summary.equals(Joining_dt)) tUpdateData.put(DBConstants.STUDENT.ATTENDENCE_SUMMARY,vStudent.Attendence_Summary);
        return tUpdateData;

    }

    public static StudentDao getStudentDao(Student vStudent){
        StudentDao tStudentDao = new StudentDao();
        tStudentDao.First_name = vStudent.First_name.trim();
        tStudentDao.Last_name = vStudent.Last_name.trim();
        tStudentDao.Contact = vStudent.Contact;
        tStudentDao.Bday = vStudent.Bday;
        tStudentDao.Batch_name = vStudent.Batch_name.trim();
        tStudentDao.Level = vStudent.Level;
        tStudentDao.Fee_status = vStudent.Fee_status;
        tStudentDao.Custom_fee = vStudent.Custom_fee;
        tStudentDao.Joining_fee_paid = vStudent.Joining_fee_paid;
        tStudentDao.Joining_fee = vStudent.Joining_fee;
        tStudentDao.Joining_fee_paid_dt= vStudent.Joining_fee_paid_dt;
        tStudentDao.Bank_ref = vStudent.Bank_ref.trim();
        tStudentDao.skills = vStudent.mSkills.trim();
        tStudentDao.Last_attended = vStudent.Last_attended;
        tStudentDao.Monthly_attendance_count = vStudent.Monthly_attendance_count;
        tStudentDao.Fee_Rcvd_dt = vStudent.Fee_Rcvd_dt;
        tStudentDao.Joining_dt = vStudent.Joining_dt;
       // tStudentDao.Attendence_Summary=vStudent.Attendence_Summary;
        return tStudentDao;
    }

    public static boolean isFeeOverdue(Student vStudent, int vCurrMM, int vCurrYYYY){

        boolean tOverdue = false;

        try {
            int paidYYYY = Integer.parseInt(TextUtils.substring(vStudent.Fee_status, 3, 7));
            int paidMM = Integer.parseInt(TextUtils.substring(vStudent.Fee_status, 0, 2));

            if((vCurrYYYY > paidYYYY) ||
                    ((vCurrYYYY == paidYYYY) && (vCurrMM > paidMM))){
                tOverdue =true;
            }
        }catch(NumberFormatException e){
            tOverdue = false;
        }

        return tOverdue;
    }

    public static final String INACTIVE = "Inactive";
    public static final String OVERDUE = "overdue";
    public static final String PAIDUP_MONTHLY = "pu_monthly";
    public static final String PAIDUP_MULTIMONTH = "pu_mmonth";
    public static final String PAIDUP_HALFMONTH = "pu_hmonth";

    public static String getFeeStatus(Student vStudent, int vCurrMM, int vCurrYYYY){

        String tCurrentStudentFeeStatus = PAIDUP_MONTHLY;

        try{

            int paidYYYY = Integer.parseInt(TextUtils.substring(vStudent.Fee_status, 3, 7));
            int paidMM = Integer.parseInt(TextUtils.substring(vStudent.Fee_status, 0, 2));

            if (((vCurrYYYY > paidYYYY) && (paidMM < 12))
                    || ((vCurrYYYY == paidYYYY) && (paidMM < vCurrMM - 1))) {
                //Inactive - exclude for all account purposes
                tCurrentStudentFeeStatus = INACTIVE;

            }else if (((vCurrYYYY > paidYYYY) && (paidMM == 12))
                    || ((vCurrYYYY == paidYYYY) && (paidMM == vCurrMM - 1))) {
                //overdue - assumption is these are monthly rate student as per original logic
                tCurrentStudentFeeStatus = OVERDUE;

            } else if (((vCurrYYYY == paidYYYY) && (vCurrMM < paidMM))
                    || (vCurrYYYY < paidYYYY)) {
                //advance paid
                tCurrentStudentFeeStatus = PAIDUP_MULTIMONTH;

            } else if ((vCurrYYYY == paidYYYY) &&
                    (vCurrMM == paidMM)) {
                //paid up
                tCurrentStudentFeeStatus = PAIDUP_MONTHLY;
            }

        }catch(NumberFormatException e){
            //Nothing
        }
        return tCurrentStudentFeeStatus;
    }

    public boolean isPresentCurrentMnth(){

        if(this.Last_attended ==null) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.Last_attended);
        int attendedMonth = cal.get(Calendar.MONTH)+1; // 0 being January
        cal.setTime(new Date());
        int currMonth = cal.get(Calendar.MONTH)+1; // 0 being January
        return (attendedMonth==currMonth );
    }

    public boolean isFeeRcvdThisMnth(){
        if(this.Fee_Rcvd_dt == null) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int currMonth = cal.get(Calendar.MONTH)+1; // 0 being January
        int currYear = cal.get(Calendar.YEAR);
        return (this.Fee_Rcvd_dt.contains(currMonth+"/"+currYear) );
    }


    public int getFeeMnths(){

        if(this.Fee_Rcvd_dt ==null ) return 1;
        int paidYYYY = Integer.parseInt(TextUtils.substring(this.Fee_status, 3, 7));
        int paidMM = Integer.parseInt(TextUtils.substring(this.Fee_status, 0, 2));
        int rcvdYYYY = Integer.parseInt(TextUtils.substring(Strings.padStart(this.Fee_Rcvd_dt,7,'0'), 3, 7));
        int rcvdMM = Integer.parseInt(TextUtils.substring(Strings.padStart(this.Fee_Rcvd_dt,7,'0'), 0, 2));

        return (paidYYYY - rcvdYYYY)*12 + (paidMM-rcvdMM) + 1;

    }

    public boolean isNewStudent(){
        if(this.Joining_dt == null) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int currMonth = cal.get(Calendar.MONTH)+1; // 0 being January
        int currYear = cal.get(Calendar.YEAR);
        return (this.Joining_dt.equalsIgnoreCase(currMonth+"/"+currYear) );
    }

    public int getLastActiveMnthToCurrentMnth(){
        if(this.Fee_status ==null ) return 1;
        int paidYYYY = Integer.parseInt(TextUtils.substring(this.Fee_status, 3, 7));
        int paidMM = Integer.parseInt(TextUtils.substring(this.Fee_status, 0, 2));
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int currMonth = cal.get(Calendar.MONTH)+1; // 0 being January
        int currYear = cal.get(Calendar.YEAR);
        return (currYear - paidYYYY)*12 + (currMonth - paidMM);
    }
}