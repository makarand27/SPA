package com.app.spa;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.app.spa.service.firestore.BatchDao;
import com.app.spa.service.repository.DataRepository;
import com.app.spa.service.repository.Student;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class ApplicationViewModel extends ViewModel {

    //My comment

    private DataRepository repo = DataRepository.getInstance();

    public void signOut(){
        repo.signOut();
    }

    public void reloadData(){
        repo.reloadData();
    }

    public HashMap<String, Student> getAllStudentData(){
        return repo.getAllStudentData();
    }
    public HashMap<String, String> getCoachData(){
        return repo.getCoachLst();
    }


    public LiveData<ArrayList<String>> getBatchList(){

        return repo.getBatchList();
    }

    public LiveData<TreeMap<String,String>> getBatchListToStdCnt(){
        return repo.getBatchListToStdCnt();
    }

    public HashMap<String,Student> getStudentNamesToStudents() {
        return repo.getStudentNamesToStudents();
    }

    public HashMap<String, Double> getFeeData(){
        return repo.getFeeData();
    }
    public HashMap<String,BatchDao> getBatchPermissionData(){
        return repo.getBatchPermissionData();
    }
    public boolean updateBatchPermissionData(BatchDao bo){
        return repo.updateBatchPermissionData(bo);
    }
    public void deleteBatchPermissionData(BatchDao bo){
        repo.deleteBatchPermissionData(bo);
    }
    public void addFee(String vFeeLevel, Double vFeeValue){
        repo.addFee(vFeeLevel, vFeeValue);
    }
    public void updateFee(String vFeeLevel, Double vFeeValue){
        repo.updateFee(vFeeLevel, vFeeValue);
    }
    public void deleteFee(String vFeeLevel){
        repo.deleteFee(vFeeLevel);
    }

    public void updateFeeStatus(final String vStudentId, String vFeeStatus,boolean halfMonth){
        repo.updateFeeStatus(vStudentId,vFeeStatus, halfMonth);
    }

    public LiveData<String> getCurrentDataSource(){
        return repo.getCurrentDataSource();
    }

    public List<CharSequence> getSortedBatchNameList(){


        return repo.getSortedBatchNameList();
    }

    public LiveData<HashMap<String, Student>> getStudentsInBatch(String vBatchName){
        return repo.getStudentsInBatch(vBatchName);
    }

    public boolean isAdminUser(){
        return repo.isAdminUser();
    }

    public List<CharSequence> getSortedFeeNameList(){
        return repo.getSortedFeeNameList();
    }
    public LiveData<Student> getStudentById(String vId){
        return repo.getStudentById(vId);
    }

    public boolean addStudent(Student vStudent){

        return repo.addStudent(vStudent);
    }
    public boolean updateStudent(Student vStudent, String vStudentId){
        return repo.updateStudent(vStudent,vStudentId);
    }

    public void deleteStudent(final String vStudentId){
        repo.deleteStudent(vStudentId);
    }

    public void updateAttendance(String vStudentId, HashMap<String,Object> hm, boolean vRefresh){
        repo.updateAttendance(vStudentId, hm, vRefresh);
    }

    public void updateJoiningKitPaid(String vStudentId, boolean vValue) {
        repo.updateJoiningKitPaid(vStudentId,vValue);
    }

    public void updateAttendanceInBatch(ArrayList<String> vStudentIdList,Date vAttendedDate,String batch, boolean vRefresh){
        repo.updateAttendanceInBatch(vStudentIdList,vAttendedDate, batch,  vRefresh);
    }


}
