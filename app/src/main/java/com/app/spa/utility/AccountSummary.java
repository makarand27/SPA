package com.app.spa.utility;


import com.app.spa.service.repository.Student;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class AccountSummary {
    boolean debug = false;
    int mMonthlyPaidStudents = 0;
    int mMultiMonthPaidStudents = 0;
    int mInactiveStudents = 0;
    int mPaymentDueStudents = 0;
    int mCustomFeeStudents = 0;
    int mTotalStudents = 0;
    int mNewlStudents = 0;
    int mMultiMnthFeeInPrevMnth;
    double mExpectedTotal = 0;
    double mActualTotal = 0;
    int halfMonthPaidStrudent = 0;
    HashSet mLevelSet = new HashSet();



    HashMap<String, Student> mStudentMap;

    HashMap<String, Integer> mCustomFeeMapExpected = new HashMap<>();
    HashMap<String, Integer> mCustomFeeMapActual = new HashMap<>();

    HashMap<String, Integer> mLevelToStudentsCountExpectedMap = new HashMap<>();
    String[] mInputFeeMap;
    HashMap<String, Double> mFeeMap = new HashMap<>();
    HashMap<String, Integer> mFeeToStudentCountMap = new HashMap<>();
    HashMap<String, Integer> mHalfFeeToStudentCountMap = new HashMap<>();
    StringBuffer mZeroFeeStdSet = new StringBuffer();


    public AccountSummary(HashMap<String, Student> vStudentMap, String[] vFeeMap) {
        mStudentMap = vStudentMap;
        mInputFeeMap = vFeeMap;
    }

    public AccountSummary(HashMap<String, Student> vStudentMap, HashMap<String, Double> vFeeMap) {

        mStudentMap = vStudentMap;
        mFeeMap = vFeeMap;
    }

    public String getAccountSummary() {

        resetData();
        calculateStats();

        StringBuffer summaryText = new StringBuffer("<html>" +
                " <head> " +
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
                "  font-family:sans-serif-medium" +
                "}" +
                "th1 {" +
                "  color: red;" +
                "  font-family:sans-serif-medium" +
                "}" +
                "</style>" +
                "</head><body>" +
                " <table> <tr>" +
                "<th colspan=\"2\"> Monthly Summary Report </th></tr>");
        summaryText.append("<tr><td><b>a </b>Monthly Students</td><td> " + mMonthlyPaidStudents + "</td></tr>");
        summaryText.append("<tr><td><b>b </b>M-Month Students (Curr Mnth) </td><td> " + mMultiMonthPaidStudents + "</td></tr>");
        summaryText.append("<tr><td><b>c </b>Unpaid till date </td><td> " + mPaymentDueStudents + "</td></tr>");
        summaryText.append("<tr><td><b>d </b>Curr Std Paid(a+b) </td><td> <b>" + (mMonthlyPaidStudents + mMultiMonthPaidStudents) + "</b></td></tr>");
        summaryText.append("<tr><td><th1>Actual Total(a+b) </th1></td><td><th1><b>" + mActualTotal + "</b></th1></td></tr>");
        summaryText.append("<tr><td>Rnt(27%) </td><td><b>" + String.format("%.2f", mActualTotal * 0.27) + "</td></tr>" );
        summaryText.append("<tr><td>Sal(35%)</td><td><b>" + String.format("%.2f", mActualTotal * 0.35) + "</b></td></tr>");
        summaryText.append("<tr><td><th1>Expected Total(a+b+c)</th1></td><td><th1> <b>" + mExpectedTotal + "</b></th1></td></tr>");
        summaryText.append("<tr><td><b>e </b>Special Fees Students  </td><td>" + mCustomFeeStudents + "</td></tr>");
        summaryText.append("<tr><td><b>e.1 </b>Half Month Fees Students  </td><td>" + halfMonthPaidStrudent + "</td></tr>");

        summaryText.append("<tr><td><b>f </b>M-Month Students (Prev Mnth)</td><td> " + mMultiMnthFeeInPrevMnth + "</td></tr>");
        summaryText.append("<tr><td><th1><b>g </b>Total Students (a+b+c+f) </th1></td><td><th1><b>" + (mMonthlyPaidStudents + mMultiMonthPaidStudents + mPaymentDueStudents + mMultiMnthFeeInPrevMnth) + "</b></th1></td></tr>");
        summaryText.append("<tr><td><b>ES </td><td><b>" + ((mMonthlyPaidStudents + mMultiMonthPaidStudents + mPaymentDueStudents + mMultiMnthFeeInPrevMnth) - mNewlStudents) + "</td></tr>");
        summaryText.append("<tr><td><b>NS</b></td><td>" + mNewlStudents + "</td></tr>");
        summaryText.append("<tr><td><b>h </b>UnSubscribed </b></td><td> " + mInactiveStudents + "</td></tr>");

        summaryText.append("<tr><td colspan=\"2\"><b><u>Fee Levels\t: Students</u></b></td></tr><tr><td colspan=\"2\">");

        ArrayList<String> tFeeArray = new ArrayList(mFeeToStudentCountMap.keySet());
        Collections.sort(tFeeArray);
        for (String tFname : tFeeArray) {
            Integer tCount = mFeeToStudentCountMap.get(tFname);
            String tLine = String.format("</b>%-20s<b>:</b>\t%d ",tFname, tCount);
            summaryText.append(tFname.equals("0") ? mZeroFeeStdSet.toString() : tLine + "\t||\t ");//

        }
        summaryText.append("</td></tr></table></body></html>");

        return summaryText.toString();
    }

    private void addToFeeStudentCount(String vFeeName) {
        if (debug) System.out.println("vFeeName " + vFeeName);
        if (mFeeToStudentCountMap.containsKey(vFeeName)) {
            int tCount = mFeeToStudentCountMap.get(vFeeName);
            mFeeToStudentCountMap.put(vFeeName, ++tCount);
        } else {
            mFeeToStudentCountMap.put(vFeeName, 1);
        }

    }

    private void addToHalfMonthFeeStudentCount(String vFeeName) {
        if (debug) System.out.println("vFeeName " + vFeeName);
        if (mHalfFeeToStudentCountMap.containsKey(vFeeName)) {
            int tCount = mHalfFeeToStudentCountMap.get(vFeeName);
            mHalfFeeToStudentCountMap.put(vFeeName, ++tCount);
        } else {
            mHalfFeeToStudentCountMap.put(vFeeName, 1);
        }

    }

    private void resetData() {
        mCustomFeeMapExpected.clear();
        mCustomFeeMapActual.clear();
        mLevelToStudentsCountExpectedMap.clear();
        mFeeToStudentCountMap.clear();

        mMonthlyPaidStudents = 0;
        mMultiMonthPaidStudents = 0;
        mMultiMnthFeeInPrevMnth = 0;
        mInactiveStudents = 0;
        mPaymentDueStudents = 0;
        mCustomFeeStudents = 0;
        mTotalStudents = 0;
        mExpectedTotal = 0;
        mActualTotal = 0;

    }


    private void calculateStats() {

        mTotalStudents = mStudentMap.size();

        Calendar cal1 = Calendar.getInstance();
        int curMM = cal1.get(Calendar.MONTH) + 1;
        int curYYYY = cal1.get(Calendar.YEAR);


        Set keySet = mStudentMap.keySet();
        Iterator keyIterator = keySet.iterator();

        while (keyIterator.hasNext()) {

            Student tStudent = mStudentMap.get((String) keyIterator.next());
            String tCurrentStudentFeeStatus = Student.getFeeStatus(tStudent, curMM, curYYYY);
            mLevelSet.add(tStudent.Level);


            if (debug)
                System.out.println("tStudent.isPresentCurrentMnth() " + tStudent.isPresentCurrentMnth() + "   dtuf " + tStudent.getFullName());
            if (tCurrentStudentFeeStatus.equals(Student.INACTIVE)) {
                mInactiveStudents++;
            } else {

                //Active students
                if (tCurrentStudentFeeStatus.equals(Student.OVERDUE) && tStudent.isPresentCurrentMnth()) {
                    mPaymentDueStudents++;

                    if (!tStudent.Custom_fee.isEmpty()) {

                        if (tStudent.Custom_fee.equals("0"))
                            mZeroFeeStdSet.append(tStudent.getFullName());
                        addToFeeStudentCount(tStudent.Custom_fee);
                        mCustomFeeStudents += 1;

                        try {

                            mExpectedTotal += Double.parseDouble(tStudent.Custom_fee);
                            if (debug)
                                System.out.println(" student 1 " + tStudent.getFullName() + " mExpectedTotal " + mExpectedTotal);

                            System.out.println("Student 11 " + tStudent.getFullName() + " add "+ Double.parseDouble(tStudent.Custom_fee));
                            //System.out.println("Expected 11 " +  mExpectedTotal);
                        } catch (NumberFormatException e) {
                            //ignore - bad data
                        }
                    } else {

                        if("Y".equals(tStudent.Is_Half_paid)){
                            mExpectedTotal += (mFeeMap.get(tStudent.Level)/2);
                            halfMonthPaidStrudent++;
                        } else {
                            mExpectedTotal += mFeeMap.get(tStudent.Level);
                        }

                        if (debug)
                            System.out.println("Student 22 " + tStudent.getFullName() + " add " + mFeeMap.get(tStudent.Level));
                        //System.out.println("Expected 22 " +  mExpectedTotal);
                        if (String.format("%.0f", mFeeMap.get(tStudent.Level)).equals("0"))
                            mZeroFeeStdSet.append(tStudent.getFullName());

                        addToFeeStudentCount(String.format("%.0f", mFeeMap.get(tStudent.Level)));


                    }

                } else if (tCurrentStudentFeeStatus.equals(Student.PAIDUP_MONTHLY)
                        && tStudent.isFeeRcvdThisMnth()) {

                    //This could be a monthly payment paid up this month or a 3 or 6 month payment made earlier ending this month
                    //but with no additional data on actual payment date stored yet - this will have to do for now
                    mMonthlyPaidStudents++;
                    if (debug) System.out.println("Multi month " + tStudent.getFullName());
                    if (!tStudent.Custom_fee.isEmpty()) {

                        mCustomFeeStudents += 1;
                        if (tStudent.Custom_fee.equals("0"))
                            mZeroFeeStdSet.append(tStudent.getFullName());

                        addToFeeStudentCount(tStudent.Custom_fee);
                        try {
                            mExpectedTotal += Double.parseDouble(tStudent.Custom_fee);
                            mActualTotal += Double.parseDouble(tStudent.Custom_fee);
                            if (debug)
                                System.out.println("Student 33 " + tStudent.getFullName() + " add " + Double.parseDouble(tStudent.Custom_fee));
                        } catch (NumberFormatException e) {
                            //ignore - bad data
                        }
                    } else {
                        try {

                            if("Y".equals(tStudent.Is_Half_paid)){
                                mExpectedTotal += ( mFeeMap.get(tStudent.Level)/2);
                                mActualTotal += (mFeeMap.get(tStudent.Level)/2);
                                halfMonthPaidStrudent++;
                            } else {
                                mExpectedTotal += mFeeMap.get(tStudent.Level);
                                mActualTotal += mFeeMap.get(tStudent.Level);
                            }
                            if (debug)
                                System.out.println("Student 44 " + tStudent.getFullName() + " add " + mFeeMap.get(tStudent.Level));
                            //System.out.println("Expected 44 " +  mExpectedTotal);
                            if (String.format("%.0f", mFeeMap.get(tStudent.Level)).equals("0"))
                                mZeroFeeStdSet.append(tStudent.getFullName());

                            addToFeeStudentCount(String.format("%.0f", mFeeMap.get(tStudent.Level)));
                        } catch (NumberFormatException e) {

                        }
                    }

                } else if (tCurrentStudentFeeStatus.equals(Student.PAIDUP_MULTIMONTH)
                        && tStudent.isFeeRcvdThisMnth()) {

                    //Two issues with this
                    //1) we dont know if this payment was maade this month
                    //2) We dont know how much many months it was paid for so we only calculate for the one month.
                    mMultiMonthPaidStudents++;
                    if (debug)
                        System.out.println("mmulitpmonth pwid this month name MM " + tStudent.getFullName());

                    if (!tStudent.Custom_fee.isEmpty()) {
                        mCustomFeeStudents += 1;
                        if (tStudent.Custom_fee.equals("0"))
                            mZeroFeeStdSet.append(tStudent.getFullName());

                        addToFeeStudentCount(tStudent.Custom_fee);
                        try {

                            mExpectedTotal += tStudent.getFeeMnths() * Double.parseDouble(tStudent.Custom_fee);
                            mActualTotal += tStudent.getFeeMnths() * Double.parseDouble(tStudent.Custom_fee);
                            if (debug)
                                System.out.println("Student 55 " + tStudent.getFullName() + " add " + tStudent.getFeeMnths() * Double.parseDouble(tStudent.Custom_fee));
                            //System.out.println("Expected 55 " +  mExpectedTotal);
                        } catch (NumberFormatException e) {
                            //ignore - bad data
                        }
                    } else {

                        int tempConcession = AppConstants.THREE_MONTH_CONCESSION;
                        if (tStudent.getFeeMnths() == 6)
                            tempConcession = AppConstants.SIX_MONTH_CONCESSION;
                        try {
                            mExpectedTotal += tStudent.getFeeMnths() * mFeeMap.get(tStudent.Level) - tempConcession;
                            mActualTotal += tStudent.getFeeMnths() * mFeeMap.get(tStudent.Level) - tempConcession;
                            if (debug)
                                System.out.println("Student 66 " + (tStudent.getFeeMnths() * mFeeMap.get(tStudent.Level) - tempConcession));
                            // System.out.println("Expected 66 " +  mExpectedTotal);
                        } catch (Exception e) {

                        }
                        if (String.format("%.0f", mFeeMap.get(tStudent.Level)).equals("0"))
                            mZeroFeeStdSet.append(tStudent.getFullName());

                        addToFeeStudentCount(String.format("%.0f", mFeeMap.get(tStudent.Level)));
                    }

                } else if (tCurrentStudentFeeStatus.equals(Student.PAIDUP_MULTIMONTH)
                        || tCurrentStudentFeeStatus.equals(Student.PAIDUP_MONTHLY)
                        && !tStudent.isFeeRcvdThisMnth()) {
                    mMultiMnthFeeInPrevMnth++;

                    if (mFeeMap.get(tStudent.Level) != null
                            && !tStudent.Custom_fee.isEmpty()) {

                        if (debug)
                            System.out.println(" Case 1.1.1 student level is " + mFeeMap.get(tStudent.Level) + " full name is " + tStudent.getFullName());
                        if (String.format("%.0f", mFeeMap.get(tStudent.Level)).equals("0"))
                            mZeroFeeStdSet.append(tStudent.getFullName());

                        addToFeeStudentCount(String.format("%.0f", mFeeMap.get(tStudent.Level)));
                    } else{

                        if (debug)
                            System.out.println("Case 1.2.2 " + tStudent.getFullName() + " no custom fee" );

                        if (String.format("%.0f", mFeeMap.get(tStudent.Level)).equals("0"))
                            mZeroFeeStdSet.append(tStudent.getFullName());
                        addToFeeStudentCount(String.format("%.0f", mFeeMap.get(tStudent.Level)));

                    }

                }
                // In any case above add the joining fee based on flags
                SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                if (debug)
                    System.out.println("Student 88 " + tStudent.getFullName() + " tStudent.Joining_fee_paid " + tStudent.Joining_fee_paid);


                if (tStudent.Joining_fee_paid
                        && formatter.format(date).equals(tStudent.Joining_fee_paid_dt)) {

                    try {
                        if (tStudent.Joining_fee == null || (tStudent.Joining_fee != null && tStudent.Joining_fee.equals(""))) {
                            tStudent.Joining_fee = "0";
                        }
                        if (debug)
                            System.out.println("new student iisss " + tStudent.getFullName() + "Joining_fee_paid_dt " + tStudent.Joining_fee_paid_dt);
                        mNewlStudents++;
                        mActualTotal += Double.parseDouble(tStudent.Joining_fee.trim());
                        mExpectedTotal += Double.parseDouble(tStudent.Joining_fee.trim());
                        if (debug) System.out.println("Student 99 " + tStudent.getFullName());
                        //System.out.println("Expected 77 " + mExpectedTotal);
                    } catch (NumberFormatException e) {

                    }
                } else if (!tStudent.Joining_fee_paid) {

                    if (debug)
                        System.out.println("New Student 1010 " + tStudent.getFullName() + " tStudent.Joining_fee_paid  " + tStudent.Joining_fee_paid);

                    mNewlStudents++;

                    // System.out.println(" studnet 0000 " + tStudent.getFullName() );

                    //System.out.println("111" + mExpectedTotal);


                    // mExpectedTotal += tStudent.Joining_fee==""?AppConstants.JOINING_FEE: Double.parseDouble(tStudent.Joining_fee);
                    //System.out.println("2222" + mExpectedTotal);

                }

                //System.out.println("Student FINAL EXP " + tStudent.getFullName() + "  mExpectedTotal "+ mExpectedTotal);


            }
        }

    }

}
