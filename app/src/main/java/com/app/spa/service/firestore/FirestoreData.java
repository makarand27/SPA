package com.app.spa.service.firestore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.spa.service.repository.Student;
import com.google.common.collect.ListMultimap;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.ArrayListMultimap;


public class FirestoreData {

    public static final boolean isDev = false;
    public static final boolean loadProdData = false;


    private static final HashMap<String, String> mAdminUsers = new HashMap();

    private final HashMap<String, Student> mStudentIdToStudentMap = new HashMap<>();
    private final HashMap<String, Student> mStudenNamesToStudentMap = new HashMap<>();
    private final HashMap<String, String> mStudentNametoIdMap = new HashMap<String, String>();

    private final ListMultimap<String, Student> mBatchNameToStudentsMap = ArrayListMultimap.create();

    private static HashMap<String, Double> mFeeMap = new HashMap<>();
    private static final HashMap<String, String> mFeeLevelToFeeIdMap = new HashMap<>();
    private static List<CharSequence> mSortedFeeNameList = new ArrayList<>();
    private static final HashMap<String, String> mCoachMap = new HashMap<>();
    private static final HashMap<String, BatchDao> mBatchPermissionsMap = new HashMap<>();

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static FirestoreData mFireStoreData = null;

    private static MutableLiveData<String> mCurrentUser = new MutableLiveData<>();
    private static MutableLiveData<Student> mCurrentStudent = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> mBatchList = new MutableLiveData<>();
    private MutableLiveData<TreeMap<String, String>> mBatchList2StdCnt = new MutableLiveData<>();
    private MutableLiveData<HashMap<String, Student>> mStudentsInBatch = new MutableLiveData<>();
    private MutableLiveData<String> mCurrentDataSource = new MutableLiveData<>();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();


    private FirestoreData() {
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser.setValue(mAuth.getCurrentUser().getEmail());
        mCurrentDataSource.setValue(DBConstants.SOURCE_NOTSET);

        LoadRoleData();
        LoadFeeData();
        LoadCoachData();
        //LoadStudentData();
        LoadBatchPermissionsData();

    }

    public static FirestoreData getInstance() {

        if (mAuth.getCurrentUser() == null) return null;

        if (mFireStoreData == null) {
            synchronized (FirestoreData.class) {
                mFireStoreData = new FirestoreData();
            }
        }
        return mFireStoreData;

    }

    public static FirebaseAuth getFirebaseAuthInstance() {
        return mAuth;
    }

    public LiveData<String> getCurrentDataSource() {
        return mCurrentDataSource;
    }


    public HashMap<String, String> getCoachData() {
        return mCoachMap;
    }

    public static boolean isAdminUser() {
        if (mAuth.getCurrentUser() != null) {
            return mAdminUsers.containsValue(mAuth.getCurrentUser().getEmail());
        } else {
            return false;
        }
    }

    private static void LoadRoleData() {

        mAdminUsers.clear();

        db.collection(isDev ? DBConstants.DEV_ADMIN_PATH : DBConstants.ADMIN_PATH)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tUserEmailId = document.getData().get(DBConstants.ADMIN.ID).toString();
                            mAdminUsers.put(document.getId(), tUserEmailId);
                        }
                    }
                });
    }

    public HashMap<String, Double> getFeeData() {

        return mFeeMap;
    }

    public HashMap<String, BatchDao> getBatchPermissionData() {

        return mBatchPermissionsMap;
    }


    public List<CharSequence> getSortedFeeNameList() {
        if (mSortedFeeNameList.size() == 0) {
            ArrayList<String> tFeeLevelNames = new ArrayList(mFeeMap.keySet());
            //Only for display
            tFeeLevelNames.add(""); //Add dummy blank entry to handle no level cases
            Collections.sort(tFeeLevelNames);
            mSortedFeeNameList = new ArrayList(tFeeLevelNames);
        }

        return mSortedFeeNameList;
    }

    public void deleteFee(String vFeeLevel) {
        if (!mFeeLevelToFeeIdMap.containsKey(vFeeLevel)) {
            return;
        }

        final String tFeeId = mFeeLevelToFeeIdMap.get(vFeeLevel);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (tFeeId != null) {
            db.collection(isDev ? DBConstants.DEV_FEES_PATH : DBConstants.FEES_PATH).document(tFeeId)
                    .delete()
                    .addOnSuccessListener(aVoid -> LoadFeeData())
                    .addOnFailureListener(e -> {
                        //TODO
                    });
        }
    }


    public void updateFee(String vFeeLevel, Double vFeeValue) {

        if (!mFeeLevelToFeeIdMap.containsKey(vFeeLevel)) {
            return;
        }

        final String tFeeId = mFeeLevelToFeeIdMap.get(vFeeLevel);

        FeesDao tDao = new FeesDao();
        tDao.Fee_level = vFeeLevel;
        tDao.Fee_amount = vFeeValue;

        DocumentReference tDocumentRef = db.collection(isDev ? DBConstants.DEV_FEES_PATH : DBConstants.FEES_PATH).document(tFeeId);
        if (tDocumentRef != null) {
            tDocumentRef.update(DBConstants.FEES.FEE_AMOUNT, vFeeValue)
                    .addOnSuccessListener(aVoid -> LoadFeeData())
                    .addOnFailureListener(e -> {
                        //TODO
                    });
        }


    }

    public void addFee(String vFeeLevel, Double vFeeValue) {

        if (mFeeMap.containsKey(vFeeLevel)) {
            return;
        }

        FeesDao tDao = new FeesDao();
        tDao.Fee_level = vFeeLevel;
        tDao.Fee_amount = vFeeValue;

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference newBatchRef = db.collection(isDev ? DBConstants.DEV_FEES_PATH : DBConstants.FEES_PATH).document();
        newBatchRef.set(tDao);
        LoadFeeData();

    }

    private static void LoadFeeData() {
        mFeeMap.clear();
        mFeeLevelToFeeIdMap.clear();
        mSortedFeeNameList.clear();

        CollectionReference feesRef = db.collection(isDev ? DBConstants.DEV_FEES_PATH : DBConstants.FEES_PATH);
        Query query = feesRef.orderBy(DBConstants.FEES.FEE_LEVEL);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tFeeLevel = document.getString(DBConstants.FEES.FEE_LEVEL);
                            Double tFeeAmount = document.getDouble(DBConstants.FEES.FEE_AMOUNT);
                            mFeeMap.put(tFeeLevel, tFeeAmount);
                            mFeeLevelToFeeIdMap.put(tFeeLevel, document.getId());
                        }
                    }
                });
    }

    private void LoadBatchPermissionsData() {
        mBatchPermissionsMap.clear();
        CollectionReference batchPermissionsM = db.collection(isDev ? DBConstants.DEV_BATCHPERMISSIONS_PATH : DBConstants.BATCHPERMISSIONS_PATH);
        batchPermissionsM.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            LoadStudentData();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String batch_name = document.getString(DBConstants.BATCH.batch_name);
                                String owners = document.getString(DBConstants.BATCH.permission);
                                BatchDao bo = new BatchDao();
                                bo.batch_name = batch_name;
                                bo.permission = owners;
                                bo.id = document.getId();
                                mBatchPermissionsMap.put(batch_name, bo);
                            }
                            LoadStudentData();
                        }
                    }
                });


    }

    private static void LoadCoachData() {
        mCoachMap.clear();

        CollectionReference coachref = db.collection(isDev ? DBConstants.DEV_COACH_PATH : DBConstants.COACH_PATH);
        // Query query = coachref.orderBy(DBConstants.COACH.COUNT);

        coachref.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tCoachEmail = document.getString(DBConstants.COACH.EMAIL);
                            String tCoachCnt = document.getString(DBConstants.COACH.COUNT);
                            String tCoachDt = document.getString(DBConstants.COACH.DATE);
                            mCoachMap.put(tCoachEmail, tCoachCnt + ";" + tCoachDt);
                        }
                    }
                });
    }

    public final void reloadData() {
        mStudentIdToStudentMap.clear();
        mStudentNametoIdMap.clear();
        mBatchNameToStudentsMap.clear();
        mStudenNamesToStudentMap.clear();
        mBatchList.getValue().clear();
        LoadBatchPermissionsData();
    }

    public void LoadStudentDataFromProd(boolean isDev) {

        db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH)
                .get()
                .addOnCompleteListener(task -> {
                    int cnt = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Student tStudent = new Student(document.toObject(StudentDao.class));
                        addStudent(tStudent);
                    }
                });

    }

    private void LoadStudentData() {

        mBatchNameToStudentsMap.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        Date threeYearsAgo = calendar.getTime();
        Timestamp timestamp = new Timestamp(threeYearsAgo);

        Query query = db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).whereGreaterThan("Last_attended", timestamp);

        //Query queryNull = db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).whereEqualTo("Last_attended", null);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String tDataSource = task.getResult().getMetadata().isFromCache() ? DBConstants.SOURCE_CACHE : DBConstants.SOURCE_SERVER;
                        mCurrentDataSource.setValue(tDataSource);

                        int totalSize = task.getResult().size();
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Student tStudent = new Student(document.toObject(StudentDao.class));
                            tStudent.Id = document.getId();
                            if(totalSize == 0) break;
                            if (!isPermittedToViewBatch(tStudent.Batch_name)) {
                                totalSize--;
                                continue;
                            }
                            if (!mAuth.getCurrentUser().getEmail().contains(DBConstants.ADMIN.ADMIN_WITH_NO_FILTER) &&
                                    tStudent.getLastActiveMnthToCurrentMnth() > 2) {
                                totalSize--;
                                continue;
                            }
                            mStudentIdToStudentMap.put(document.getId(), tStudent);
                            mStudentNametoIdMap.put(tStudent.First_name + " " + tStudent.Last_name, document.getId());
                            mBatchNameToStudentsMap.put(tStudent.Batch_name.trim(), tStudent);
                            mStudenNamesToStudentMap.put(tStudent.getFullName(), tStudent);

                            totalSize--;


                        }
                        ArrayList<String> tBatchNameList = new ArrayList<>(mBatchNameToStudentsMap.keySet());
                        Collections.sort(tBatchNameList);
                        mBatchList.setValue(tBatchNameList);
                        TreeMap<String, String> tmpMap = new TreeMap<>();
                        for (String batchName : mBatchNameToStudentsMap.keySet()) {
                            tmpMap.put(batchName, "" + mBatchNameToStudentsMap.get(batchName).size());
                        }
                        mBatchList2StdCnt.setValue(tmpMap);
                        refreshStudentsInBatchMap();

                    }
                });

    }

    public LiveData<ArrayList<String>> getBatchList() {

        return mBatchList;
    }

    public LiveData<TreeMap<String, String>> getBatchListToStdCnt() {
        return mBatchList2StdCnt;
    }


    public List<CharSequence> getSortedBatchNameList() {

        return new ArrayList(mBatchList.getValue());
    }

    public LiveData<Student> getStudentById(String vId) {
        mCurrentStudent.setValue(mStudentIdToStudentMap.get(vId));
        return mCurrentStudent;
    }
//    public Student getStudentById(String vId){
//
//        return mStudentIdToStudentMap.get(vId);
//    }

    public HashMap<String, Student> getAllStudentData() {
        return mStudentIdToStudentMap;
    }

    public static boolean isPermittedToViewBatch(String batchName){

        if(isAdminUser()) return true;

        BatchDao batchDao = mBatchPermissionsMap.get(batchName);
        if (!mBatchPermissionsMap.containsKey(batchName) || (
                batchDao != null && !batchDao.permission.contains(mAuth.getCurrentUser().getEmail()))) {
            return false;
        }

        return true;

    };

    public HashMap<String, Student> getStudentNamesToStudents() {
        return mStudenNamesToStudentMap;
    }


    private void refreshStudentsInBatchMap() {
        if (mStudentsInBatch.getValue() == null) {
            return;
        }

        if (mStudentsInBatch.getValue().size() > 0) {
            for (String key : mStudentsInBatch.getValue().keySet()) {
                Student tStudent = mStudentsInBatch.getValue().get(key);
                if (tStudent != null) {
                    getStudentsInBatch(tStudent.Batch_name);
                    break;
                }
            }
        }
    }

    public LiveData<HashMap<String, Student>> getStudentsInBatch(String pBatchName) {

        HashMap<String, Student> tmpMap = new HashMap<>();

        List<Student> tStudentList = mBatchNameToStudentsMap.get(pBatchName);
        if (!tStudentList.isEmpty()) {
            for (Student key : tStudentList) {

                tmpMap.put(key.getFullName(), key);
            }
        }
        mStudentsInBatch.setValue(tmpMap);

        return mStudentsInBatch;
    }


    private boolean validateStudent(Student vStudent) {
        boolean tResult = true;
        if (vStudent.First_name == null || vStudent.First_name.isEmpty()) {
            tResult = false;
        }
        if (vStudent.Batch_name == null || vStudent.Batch_name.isEmpty()) {
            tResult = false;
        }

        return tResult;
    }

    public boolean addStudent(Student vStudent) {

        if (!validateStudent(vStudent)) {
            return false;
        }

        if (mStudentNametoIdMap.containsKey(vStudent.getFullName())) return false;

        StudentDao tStudentDao = Student.getStudentDao(vStudent);

        DocumentReference newBatchRef = db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).document();
        newBatchRef.set(tStudentDao);
        return true;
    }

    public void deleteStudent(final String vStudentId) {

        if (vStudentId != null) {
            db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).document(vStudentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> reloadData())
                    .addOnFailureListener(e -> {
                        //TODO

                    });
        }
    }

    public void deleteBatch(BatchDao bo) {

        if (bo != null) {
            db.collection(isDev ? DBConstants.DEV_BATCHPERMISSIONS_PATH : DBConstants.BATCHPERMISSIONS_PATH).document(bo.id)
                    .delete()
                    .addOnSuccessListener(
                            aVoid -> reloadData()
                    )
                    .addOnFailureListener(e -> {
                        //TODO

                    });
        }
    }

    public boolean addBatch(BatchDao bo) {
        DocumentReference newBatchRef = db.collection(isDev ? DBConstants.DEV_BATCHPERMISSIONS_PATH : DBConstants.BATCHPERMISSIONS_PATH).document();
        bo.id = newBatchRef.getId();
        newBatchRef.set(bo);
        return true;
    }

  /*
    public void updateCoachStats(String batch){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tDocumentRef = db.collection(DBConstants.COACH_PATH).document(mAuth.getCurrentUser().getEmail());
        if(tDocumentRef != null){
            HashMap<String,Object> tUpdateData = new HashMap<>();
            String tempStr = mCoachMap.get(mAuth.getCurrentUser().getEmail());
            String[] tokens = tempStr.split(";");

            if(tokens.length==2){

                String tempDt = tokens[1];
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                int currMonth = cal.get(Calendar.MONTH)+1; // 0 being January
                String currMonthPad = Strings.padStart(""+currMonth,2,'0');
                int currDay = cal.get(Calendar.DAY_OF_MONTH);
                String currDayPad = Strings.padStart(""+currDay,2,'0');
                int currYr = cal.get(Calendar.YEAR);

                if(!tempDt.substring(3,10).equals(currMonthPad+"/"+currYr))
                    tempStr ="";



                String[] batchTokens = tokens[0].split("~"); // get batch=couunt here
                String coachSummary ="";
                for (String temptoken : batchTokens) {
                    String[] equlToken = temptoken.split("=");/// split batch token into batch and count
                    String finalEqulString ="";
                    if (equlToken != null && equlToken.length == 2) {
                        String tempBatch = equlToken[0];
                        int tempCnt = Integer.parseInt(equlToken[1]);

                        if (tempBatch.equalsIgnoreCase(batch) && !tempDt.equals(currDayPad+"/"+currMonthPad+"/"+currYr) ) {
                            tempCnt++;
                        }else if(!tempBatch.equalsIgnoreCase(batch)) {
                            tempBatch = batch;
                            tempCnt=1;
                        }

                        coachSummary+= (tempBatch+"="+tempCnt+"~");
                    }else{
                        coachSummary = batch+"="+"1";
                    }

                }

                /// update batch heree
                tempDt = currDayPad+"/"+currMonthPad+"/"+currYr;
                tUpdateData.put(DBConstants.COACH.DATE,tempDt);
                tUpdateData.put(DBConstants.COACH.COUNT, coachSummary);
                tUpdateData.put(DBConstants.COACH.EMAIL, mAuth.getCurrentUser().getEmail());
            }


            tDocumentRef.update(tUpdateData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            LoadCoachData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //TODO
                        }
                    });
        }

    }

   */

    /*
    public void updateAttendanceInBatch(ArrayList<String> vStudentId, Date vAttendedDate,String batch, boolean vRefresh){


        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        boolean bRefresh = false;

        DocumentReference tDocumentRef = null;
        WriteBatch batchWrite = db.batch();
        int counter =0;
        if(tDocumentRef != null){
            tDocumentRef.update("","")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            reloadData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //TODO
                        }
                    });
        }

        for (String key : vStudentId) {
            counter--;
            if(counter == 0){
                bRefresh = true;
            }
            tDocumentRef = db.collection(DBConstants.STUDENT_PATH).document(vStudentId);
            if (tDocumentRef == null) return;
            batchWrite.update(DBConstants.STUDENT.LAST_ATTENDED,vFieldValue);

        }

        //updateDateField(vStudentId,DBConstants.STUDENT.LAST_ATTENDED,vAttendedDate, vRefresh);
        //updateCoachStats(batch);
    }
*/
    public void updateAttendance(String vStudentId, HashMap<String,Object> sm, boolean vRefresh) {
        updateFields(vStudentId, sm, vRefresh);

        //updateCoachStats(batch);
    }

    private void updateDateField(final String vStudentId, String vFieldName, Date vFieldValue, boolean vRefresh) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tDocumentRef = db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).document(vStudentId);
        if (tDocumentRef != null) {
            tDocumentRef.update(vFieldName, vFieldValue)
                    .addOnSuccessListener(aVoid -> {
                        if (vRefresh) {
                            reloadData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        //TODO
                    });


        }

    }

    private void updateFields(final String vStudentId, HashMap<String,Object> fieldM, boolean vRefresh) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tDocumentRef = db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).document(vStudentId);
        if (tDocumentRef != null) {
            tDocumentRef.update(fieldM)
                    .addOnSuccessListener(aVoid -> {
                        if(vRefresh)reloadData();})
                    .addOnFailureListener(e -> {
                        //TODO
                    });
        }
    }
    private void updateField(final String vStudentId, String vFieldName, boolean vFieldValue) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tDocumentRef = db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).document(vStudentId);
        if (tDocumentRef != null) {
            tDocumentRef.update(vFieldName, vFieldValue)
                    .addOnSuccessListener(aVoid -> reloadData())
                    .addOnFailureListener(e -> {
                        //TODO
                    });
        }

    }

    public void updateJoiningKitPaid(String vStudentId, boolean vValue) {

        updateField(vStudentId, DBConstants.STUDENT.JOINING_FEE_PAID, vValue);
    }


    public void updateFeeStatus(final String vStudentId, String vFeeStatus, boolean halfMonth) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tDocumentRef = db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).document(vStudentId);
        if (tDocumentRef != null) {
            Map<String, Object> updateM = new HashMap<String, Object>();
            updateM.put(DBConstants.STUDENT.FEE_STATUS, vFeeStatus);
            Date today = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(today); // don't forget this if date is arbitrary
            updateM.put(DBConstants.STUDENT.FEE_RCVD_DT, cal.get(Calendar.MONTH) /* 0 being January*/ + 1 + "/" + cal.get(Calendar.YEAR));

            updateM.put(DBConstants.STUDENT.IS_HALF_PAID, halfMonth ? "Y" : "N");
            tDocumentRef.update(updateM)
                    .addOnSuccessListener(aVoid -> reloadData())
                    .addOnFailureListener(e -> {
                        //TODO
                    });

        }

    }


    public boolean updateStudent(Student vStudent, String vStudentId) {

        if (!validateStudent(vStudent)) {
            return false;
        }

        if (!mStudentNametoIdMap.containsValue(vStudentId)) {
            return false;
        }

        final Student tLocalStudent = mStudentIdToStudentMap.get(vStudentId);

        if (tLocalStudent == null) return false;


        Map<String, Object> tUpdateData = tLocalStudent.GetUpdateFields(vStudent);

        if (tUpdateData.size() == 0) {
            return false;
        }

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference newBatchRef = db.collection(isDev ? DBConstants.DEV_STUDENT_PATH : DBConstants.STUDENT_PATH).document(vStudentId);
        if (newBatchRef != null) {
            newBatchRef.update(tUpdateData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reloadData();
                }
            });
        }

        return true;

    }

    private void clearAllData() {
        //Called on SignOut
        mStudentIdToStudentMap.clear();
        mStudenNamesToStudentMap.clear();
        mStudentNametoIdMap.clear();
        mBatchNameToStudentsMap.clear();

        mAdminUsers.clear();
    }

    public void signOut() {
        clearAllData();
        mAuth.signOut();
        mFireStoreData = null;
    }
}