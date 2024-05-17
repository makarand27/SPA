package com.app.spa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.app.spa.utility.AppConstants;
import com.app.spa.service.repository.Student;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.common.base.Strings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import android.text.TextUtils;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StudentMaintActivity extends AppCompatActivity {

    TextInputEditText mFirstName;
    TextInputEditText mLastName;
   // AutoCompleteTextView mBatchName;
    TextInputEditText mContact;
    TextInputEditText mBday;
    TextInputEditText mCFee;
    TextInputEditText mJFee;
    MaterialTextView mJFeeRcdt;
    TextInputEditText mBankRef;
    TextInputEditText mSkills;
    AutoCompleteTextView mMMSpinner;
    TextInputLayout mMMSpinnerInputLayout;
    AutoCompleteTextView mYYYYSpinner;

    TextInputLayout mYYYYSpinnerInputLayout;

    AutoCompleteTextView mMMRcvdSpinner;
    TextInputLayout mMMRcvdSpinnerInputLayout;

    AutoCompleteTextView mYYYYRcvdSpinner;
    TextInputLayout mYYYYRcvdSpinnerInputLayout;

    AutoCompleteTextView mBatchSpinner;
    TextInputLayout mBatchSpinnerInputLayout;
    AutoCompleteTextView mLevelSpinner;
    TextInputLayout mLevelSpinnerInputLayout;
    CheckBox mChkBoxJoiningFee;
    ImageButton mCallButton;
    ImageButton mSaveskillButton;
    ArrayAdapter<CharSequence> mMMAdapter;
    ArrayAdapter<CharSequence> mYYYYAdapter;

    ArrayAdapter<CharSequence> mMMRcvdAdapter;
    ArrayAdapter<CharSequence> mYYYYRcvdAdapter;

    ArrayAdapter<CharSequence> mBatchListAdapter;
    ArrayAdapter<CharSequence> mLevelListAdapter;
    boolean mBExistingBatchSelected = true;

    private String mStudentId = "";
    private Student mStudentModel;

    private Context mContext;

    private ApplicationViewModel mViewModel;
    private String mCurrentMode = AppConstants.INTENT_VALUES.MAINT_ACTION_VIEW;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_studentdetails_context, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            case R.id.ab_fab_edit:
                    enableEditing();
                    mCurrentMode = AppConstants.INTENT_VALUES.MAINT_ACTION_EDIT;
                    this.invalidateOptionsMenu();
                    return(true);
            case R.id.ab_btn_update_student1:
                Intent mIntent = getIntent();
                if(AppConstants.INTENT_VALUES.MAINT_ACTION_ADD.equals(mIntent.getExtras().getString(AppConstants.INTENT_KEY.ACTION)))
                    onClickAdd();
                else
                    onClickUpdate();
                return(true);
            case R.id.ab_btn_delete_student1:
                onClickDelete();
                return(true);
    }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_maint);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getApplicationContext();

        mViewModel = ViewModelProviders.of(this).get(ApplicationViewModel.class);

        mapFields();

        Intent tIntent = getIntent();
        mCurrentMode = tIntent.getExtras().getString(AppConstants.INTENT_KEY.ACTION);

        //Initialize model
        if(mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_VIEW)
                || mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_EDIT)){

            mStudentId = tIntent.getExtras().getString(AppConstants.INTENT_KEY.STUDENT_ID);
            mViewModel.getStudentById(mStudentId).observe(this, student -> {

            });
            mStudentModel = mViewModel.getStudentById(mStudentId).getValue();
            if(mStudentModel==null){
                showValidationErrors("Something went wrong , please try again");
                return;
            }
            setFieldValuesFromModel();

            if(mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_VIEW)){
                disableEditing();
            }
        }
        else {
            mStudentModel = new Student();
        }

        setListenerActions();
        setButtonVisibility();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(!mViewModel.isAdminUser()
                || mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_ADD)
                || mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_EDIT)) {

            menu.findItem(R.id.ab_fab_edit).setEnabled(false);
            menu.findItem(R.id.ab_fab_edit).setVisible(false);
            menu.findItem(R.id.ab_btn_update_student1).setEnabled(true);
            menu.findItem(R.id.ab_btn_update_student1).setVisible(true);

        }else if(mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_VIEW)){
            menu.findItem(R.id.ab_fab_edit).setEnabled(true);
            menu.findItem(R.id.ab_fab_edit).setVisible(true);
            menu.findItem(R.id.ab_btn_update_student1).setEnabled(false);
            menu.findItem(R.id.ab_btn_update_student1).setVisible(false);

        }
            return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;

    }
    private void setFieldValuesFromModel(){

        String tFeeStatus = mStudentModel.Fee_status;

        if(tFeeStatus != null && !tFeeStatus.isEmpty()){
            //Assumption existing data will be in correct format if not empty
            int paidYYYY = Integer.parseInt(TextUtils.substring(tFeeStatus,3, 7));
            int paidMM = Integer.parseInt(TextUtils.substring(tFeeStatus,0, 2));
            String tYYYY = String.format("%04d",paidYYYY);
            String tMM = String.format("%02d",paidMM);
            mMMSpinner.setText(tMM,false);
            mYYYYSpinner.setText(tYYYY,false);
        }


        String tFeeRcvd = mStudentModel.Fee_Rcvd_dt;

        if(tFeeRcvd==null) tFeeRcvd=tFeeStatus;

        if(tFeeRcvd != null && !tFeeRcvd.isEmpty()) {

            //Assumption existing data will be in correct format if not empty
            int paidYYYY = Integer.parseInt(TextUtils.substring(Strings.padStart(tFeeRcvd,7,'0'),3, 7));
            int paidMM = Integer.parseInt(TextUtils.substring(Strings.padStart(tFeeRcvd,7,'0'),0, 2));
            String tYYYY = String.format("%04d",paidYYYY);
            String tMM = String.format("%02d",paidMM);

            mMMRcvdSpinner.setText(tMM);
            mYYYYRcvdSpinner.setText(tYYYY);
            mMMRcvdSpinner.setEnabled(false);
            mYYYYRcvdSpinner.setEnabled(false);
        }
        //Not handling year old deactivated for performance reasons - only checking month - if there is business need then can be added in
        mFirstName.setText(mStudentModel.First_name);
        mLastName.setText(mStudentModel.Last_name);
        mContact.setText(mStudentModel.Contact);
        mBday.setText(mStudentModel.Bday);
        mCFee.setText(mStudentModel.Custom_fee);
        mJFee.setText(mStudentModel.Joining_fee);
        mJFeeRcdt.setText(mStudentModel.Joining_fee_paid_dt);
        mBankRef.setText(mStudentModel.Bank_ref);
        mSkills.setText(mStudentModel.mSkills);

        mBatchSpinner.setText(mStudentModel.Batch_name,false);

        mLevelSpinner.setText(mStudentModel.Level,false);

        mChkBoxJoiningFee.setChecked(mStudentModel.Joining_fee_paid);

    }

    private void mapFields() {

        mFirstName = findViewById(R.id.txt_edit_fname);
        mLastName = findViewById(R.id.txt_edit_lname);
        mContact = findViewById(R.id.txt_edit_contact);
        mBday = findViewById(R.id.txt_edit_bday);
        mCFee = findViewById(R.id.txt_edit_cfee);
        mJFee = findViewById(R.id.txt_edit_jfee);
        mJFeeRcdt = findViewById(R.id.txt_Jfee_dt);

        mBankRef = findViewById(R.id.txt_bank_ref);
        mSkills = findViewById(R.id.txt_skill_ref);
        mMMSpinner = findViewById(R.id.sp_fee_month);
        mMMSpinnerInputLayout = findViewById(R.id.sp_fee_month1);
        mYYYYSpinner = findViewById(R.id.sp_fee_year);
        mYYYYSpinnerInputLayout = findViewById(R.id.sp_fee_year1);
        mMMRcvdSpinner =findViewById(R.id.fee_rcvd_month);
        mMMRcvdSpinnerInputLayout = findViewById(R.id.fee_rcvd_month1);
        mYYYYRcvdSpinner=findViewById(R.id.fee_rcvd_year);
        mYYYYRcvdSpinnerInputLayout = findViewById(R.id.fee_rcvd_year1);
        mBatchSpinner = findViewById(R.id.txt_edit_bname);
        mBatchSpinnerInputLayout = findViewById(R.id.txt_edit_bname1);
        mCallButton = findViewById(R.id.btn_call);
        mSaveskillButton = findViewById(R.id.btn_save_skill);
        mLevelSpinner = findViewById(R.id.sp_level);
        mLevelSpinnerInputLayout = findViewById(R.id.sp_level1);
        mChkBoxJoiningFee = findViewById(R.id.chk_joining_fee);

        mMMAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        mMMAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMMSpinner.setAdapter(mMMAdapter);

        mYYYYAdapter = ArrayAdapter.createFromResource(this,
                R.array.years_array, android.R.layout.simple_spinner_item);
        mYYYYAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYYYYSpinner.setAdapter(mYYYYAdapter);


        mMMRcvdAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        mMMRcvdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMMRcvdSpinner.setAdapter(mMMRcvdAdapter);

        mYYYYRcvdAdapter = ArrayAdapter.createFromResource(this,
                R.array.years_array, android.R.layout.simple_spinner_item);
        mYYYYRcvdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYYYYRcvdSpinner.setAdapter(mYYYYRcvdAdapter);

        mLevelListAdapter = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_spinner_item,mViewModel.getSortedFeeNameList());
        mLevelListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLevelSpinner.setAdapter(mLevelListAdapter);

        mBatchListAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, mViewModel.getSortedBatchNameList());
        mBatchListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBatchSpinner.setAdapter(mBatchListAdapter);

        //Set default values
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.MONTH,-1);
        int paidMM = cal1.get(Calendar.MONTH)+1;
        int paidYYYY = cal1.get(Calendar.YEAR);
        String tYYYY = String.format("%04d",paidYYYY);
        String tMM = String.format("%02d",paidMM);

        mMMSpinner.setText(tMM,false);
        mYYYYSpinner.setText(tYYYY);
        mMMRcvdSpinner.setText(tMM);
        mMMRcvdSpinner.setEnabled(false);
        mYYYYRcvdSpinner.setText(tYYYY);
        mYYYYRcvdSpinner.setEnabled(false);

    }

    private void setButtonVisibility() {
        //TODO:: Reverify if joining kit is rquired ever

            if (mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_VIEW)) {

                mBExistingBatchSelected = true;
                mCallButton.setEnabled(true);

            } else if (mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_ADD)) {

                mBatchSpinner.setVisibility(View.VISIBLE);
                mBExistingBatchSelected = true;
                mCallButton.setEnabled(false);

            } else if (mCurrentMode.equals(AppConstants.INTENT_VALUES.MAINT_ACTION_EDIT)) {

                mBatchSpinner.setVisibility(View.VISIBLE);
                mBExistingBatchSelected = true;
                mCallButton.setEnabled(true);

            } else {

                mCallButton.setEnabled(false);
            }



    }

    private void disableEditing() {
        mFirstName.setEnabled(false);
        mLastName.setEnabled(false);
        mContact.setEnabled(false);
        mBday.setEnabled(false);
        mCFee.setEnabled(false);
        mJFee.setEnabled(false);
        mJFeeRcdt.setEnabled(false);

        mBankRef.setEnabled(false);
        mMMSpinner.setEnabled(false);
        mMMSpinnerInputLayout.setEnabled(false);
        mYYYYSpinner.setEnabled(false);
        mYYYYSpinnerInputLayout.setEnabled(false);
        mMMRcvdSpinner.setEnabled(false);
        mMMRcvdSpinnerInputLayout.setEnabled(false);
        mYYYYRcvdSpinner.setEnabled(false);
        mYYYYRcvdSpinnerInputLayout.setEnabled(false);
        mBatchSpinner.setEnabled(false);
        mBatchSpinnerInputLayout.setEnabled(false);
        mLevelSpinner.setEnabled(false);
        mLevelSpinnerInputLayout.setEnabled(false);
        mChkBoxJoiningFee.setEnabled(false);


    }

    private void enableEditing() {
        mFirstName.setEnabled(true);
        mLastName.setEnabled(true);
        mContact.setEnabled(true);
        mBday.setEnabled(true);
        mCFee.setEnabled(true);
        mJFee.setEnabled(true);
        mJFeeRcdt.setEnabled(true);

        mBankRef.setEnabled(true);
        mMMSpinner.setEnabled(true);
        mMMSpinnerInputLayout.setEnabled(true);

        mYYYYSpinner.setEnabled(true);
        mYYYYSpinnerInputLayout.setEnabled(true);

        mMMRcvdSpinner.setEnabled(true);
        mMMRcvdSpinnerInputLayout.setEnabled(true);

        mYYYYRcvdSpinner.setEnabled(true);
        mYYYYRcvdSpinnerInputLayout.setEnabled(true);

        mBatchSpinner.setEnabled(true);
        mBatchSpinnerInputLayout.setEnabled(true);

        mLevelSpinner.setEnabled(true);
        mLevelSpinnerInputLayout.setEnabled(true);

        mChkBoxJoiningFee.setEnabled(true);


    }

    private void onClickUpdate() {

        new AlertDialog.Builder(this)
                .setTitle("Update Student")
                .setMessage("Confirm update?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    Student tStudent = new Student();
                    tStudent.First_name = mFirstName.getText().toString().trim();
                    tStudent.Last_name = mLastName.getText().toString().trim();
                    if(mBExistingBatchSelected){
                        tStudent.Batch_name = mBatchSpinner.getText().toString().trim();

                    }else{
                        tStudent.Batch_name = mBatchSpinner.getText().toString().trim();
                    }

                    tStudent.Fee_status = mMMSpinner.getText().toString() + "/" + mYYYYSpinner.getText().toString();
                    tStudent.Fee_Rcvd_dt =mMMRcvdSpinner.getText().toString() + "/" +mYYYYRcvdSpinner.getText().toString();

                    tStudent.Contact = mContact.getText().toString().trim();
                    tStudent.Bday = mBday.getText().toString().trim();
                    tStudent.Level = mLevelSpinner.getText().toString().trim();
                    tStudent.Custom_fee = mCFee.getText().toString().trim();
                    tStudent.Joining_fee=mJFee.getText().toString().trim();
                    tStudent.Bank_ref = mBankRef.getText().toString().trim();
                    tStudent.mSkills = mSkills.getText().toString().trim();
                    tStudent.Joining_fee_paid = mChkBoxJoiningFee.isChecked();

                    if(mChkBoxJoiningFee.isChecked()
                            && (mJFeeRcdt.getText()!=null && mJFeeRcdt.getText().equals(""))){
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
                        Calendar calendar = Calendar.getInstance();
                        Date date = calendar.getTime();
                        tStudent.Joining_fee_paid_dt= formatter.format(date);
                    }
                    String tErr = validateStudentData(tStudent);
                    if(!tErr.isEmpty()){
                        showValidationErrors(tErr);
                    }else {
                        mViewModel.updateStudent(tStudent, mStudentId);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();

    }
    @Override
    protected void onRestart() {
        super.onRestart();
        startActivity(getIntent());
    }

    private void onClickDelete() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Confirm deletion?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    mViewModel.deleteStudent(mStudentId);
                    //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    finish();
                })
                .setNegativeButton(android.R.string.no, null).show();

    }


    private void onClickAdd() {

        new AlertDialog.Builder(this)
                .setTitle("Add Student")
                .setMessage("Confirm Addition?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {

                    Student tStudent = new Student();
                    tStudent.First_name = mFirstName.getText().toString();
                    tStudent.Last_name = mLastName.getText().toString();
                    if(mBExistingBatchSelected){
                        tStudent.Batch_name = mBatchSpinner.getText().toString();

                    }else{
                        tStudent.Batch_name = mBatchSpinner.getText().toString();
                    }
                    tStudent.Fee_status = mMMSpinner.getText().toString() + "/" + mYYYYSpinner.getText().toString();
                    tStudent.Contact = mContact.getText().toString().trim();
                    tStudent.Bday = mBday.getText().toString().trim();
                    tStudent.Level = mLevelSpinner.getText().toString().trim();
                    tStudent.Custom_fee = mCFee.getText().toString().trim();
                    tStudent.Joining_fee = mJFee.getText().toString().trim();
                    tStudent.Joining_fee_paid = mChkBoxJoiningFee.isChecked();
                    tStudent.Bank_ref = mBankRef.getText().toString().trim();
                    tStudent.mSkills = mSkills.getText().toString().trim();
                    tStudent.Last_attended=new Date();
                    if(mChkBoxJoiningFee.isChecked() && tStudent.Joining_fee_paid_dt==null){
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
                        Calendar calendar = Calendar.getInstance();
                        Date date = calendar.getTime();
                        tStudent.Joining_fee_paid_dt= formatter.format(date);
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    int currMonth = cal.get(Calendar.MONTH)+1; // 0 being January
                    int currYear = cal.get(Calendar.YEAR);
                    tStudent.Joining_dt = currMonth + "/" + currYear;
                    tStudent.Fee_Rcvd_dt = mMMSpinner.getText().toString() + "/" + mYYYYSpinner.getText().toString();
                    String tErr = validateStudentData(tStudent);
                    if(!tErr.isEmpty()){
                        showValidationErrors(tErr);
                    }else{
                        mViewModel.addStudent(tStudent);
                        mViewModel.reloadData();
                        finish();
                    }
                    //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                })
                .setNegativeButton(android.R.string.no, null).show();

    }

    private void showValidationErrors(String vErrorMessage){
        new AlertDialog.Builder(this)
                .setTitle("Validation Checks failed - Please address")
                .setMessage(vErrorMessage)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null).show();
    }



    private String validateStudentData(Student vStudent){

        StringBuilder sb = new StringBuilder();
        if(vStudent.First_name.isEmpty()){
            sb.append("First name cannot be empty\n");
        }
        if(vStudent.Batch_name.isEmpty()){
            sb.append("Batch must be selected\n");
        }

        if(vStudent.Level.isEmpty() && vStudent.Custom_fee.isEmpty()){
            sb.append("Either Level or Custom Fee must be selected\n");
        }

        if(!vStudent.Level.isEmpty() && !vStudent.Custom_fee.isEmpty()){
            sb.append("Either Level or Custom Fee must be selected\n");
        }

        if(vStudent.Contact.isEmpty()){
            sb.append("Contact must be specified\n");
        }

        if(vStudent.Contact.length() != 10){
            sb.append("Contact must be 10 digits\n");
        }


        return sb.toString();
    }


    private void onClickSaveSkills() {

        onClickUpdate();

    }
    private void onClickCall(){

        if(mStudentModel != null){
            if(mStudentModel.Contact != null
                    && !mStudentModel.Contact.equals("")){
                Intent dial = new Intent();
                dial.setAction("android.intent.action.DIAL");
                try {
                    dial.setData(Uri.parse("tel:"+ mStudentModel.Contact));
                    startActivity(dial);
                    //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                } catch (Exception e) {
                    //TODO - message or ignore?
                }
            }

        }
    }


    private void onClickEdit() {

        if (mViewModel.isAdminUser()) {
            //Enable deletes
            Intent tMaintIntent = new Intent(StudentMaintActivity.this, StudentMaintActivity.class);

            tMaintIntent.putExtra(AppConstants.INTENT_KEY.ACTION, AppConstants.INTENT_VALUES.MAINT_ACTION_EDIT);
            tMaintIntent.putExtra(AppConstants.INTENT_KEY.STUDENT_ID, mStudentId);
            startActivity(tMaintIntent);
            finish();
        }
    }

    private void setListenerActions() {

        mCallButton.setOnClickListener(view -> onClickCall());
        mSaveskillButton.setOnClickListener(view -> onClickSaveSkills());

    }


}
