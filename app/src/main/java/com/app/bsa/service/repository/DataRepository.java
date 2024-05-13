package com.app.bsa.service.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.bsa.service.firestore.BatchDao;
import com.app.bsa.service.firestore.FirestoreData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataRepository{

    private static DataRepository mDataRepository;
    private static FirestoreData mFirestoreData;


    private DataRepository(){
        mFirestoreData = FirestoreData.getInstance();
    }

    public static DataRepository getInstance(){
        if(mDataRepository == null){
            mDataRepository =  new DataRepository();
        }
        return mDataRepository;

    }
    public LiveData<String> getCurrentDataSource(){
        return mFirestoreData.getCurrentDataSource();
    }

    public void signOut(){
        mFirestoreData.signOut();
    }

    public void reloadData(){
        mFirestoreData.reloadData();
    }

    public HashMap<String, Student> getAllStudentData(){
        return mFirestoreData.getAllStudentData();
    }

    public HashMap<String, String> getCoachLst(){
        return mFirestoreData.getCoachData();
    }
    public LiveData<ArrayList<String>>getBatchList(){
     return mFirestoreData.getBatchList();
    }

    public LiveData<TreeMap<String,String>>getBatchListToStdCnt(){
        return mFirestoreData.getBatchListToStdCnt();
    }
    public List<CharSequence> getSortedBatchNameList(){
        return mFirestoreData.getSortedBatchNameList();
    }

    public LiveData<HashMap<String, Student>> getStudentsInBatch(String vBatchName){
        return mFirestoreData.getStudentsInBatch(vBatchName);
    }
    public HashMap<String,Student> getStudentNamesToStudents() {
        return mFirestoreData.getStudentNamesToStudents();
    }

    public boolean isAdminUser(){
        return mFirestoreData.isAdminUser();
    }

    public HashMap<String, Double> getFeeData(){
        return mFirestoreData.getFeeData();
    }

    public HashMap<String,BatchDao> getBatchPermissionData(){
        return mFirestoreData.getBatchPermissionData();
    }
    public boolean updateBatchPermissionData(BatchDao bo){
        return mFirestoreData.addBatch(bo);
    }
    public void deleteBatchPermissionData(BatchDao bo){
            mFirestoreData.deleteBatch(bo);
    }
    public void addFee(String vFeeLevel, Double vFeeValue){
        mFirestoreData.addFee(vFeeLevel, vFeeValue);
    }
    public void updateFee(String vFeeLevel, Double vFeeValue){
        mFirestoreData.updateFee(vFeeLevel, vFeeValue);
    }
    public void deleteFee(String vFeeLevel){
        mFirestoreData.deleteFee(vFeeLevel);
    }

    public void updateFeeStatus(final String vStudentId, String vFeeStatus,boolean halfMonth){
        mFirestoreData.updateFeeStatus(vStudentId,vFeeStatus,halfMonth);
    }

    public List<CharSequence> getSortedFeeNameList(){

        return mFirestoreData.getSortedFeeNameList();
    }
    public LiveData<Student> getStudentById(String vId){

        return mFirestoreData.getStudentById(vId);
    }

    public boolean addStudent(Student vStudent){

        return mFirestoreData.addStudent(vStudent);
    }

    public boolean updateStudent(Student vStudent, String vStudentId){
        return mFirestoreData.updateStudent(vStudent,vStudentId);
    }

    public void updateAttendance(String vStudentId, HashMap<String,Object> hm, boolean vRefresh){
        mFirestoreData.updateAttendance(vStudentId, hm, vRefresh);
    }
     public void deleteStudent(final String vStudentId){
        mFirestoreData.deleteStudent(vStudentId);
    }

    public void updateJoiningKitPaid(String vStudentId, boolean vValue) {
        mFirestoreData.updateJoiningKitPaid(vStudentId,vValue);
    }

    public void updateAttendanceInBatch(ArrayList<String> vStudentIdList,Date vAttendedDate,String batch, boolean vRefresh){
        //mFirestoreData.updateAttendanceInBatch(vStudentIdList,vAttendedDate,batch,vRefresh);
    }
}
