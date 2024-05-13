package com.app.bsa;

import static com.app.bsa.utility.AppConstants.PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.app.bsa.adapter.BatchListAdapter;
import com.app.bsa.adapter.StudentListAdapter;
import com.app.bsa.service.firestore.DBConstants;
import com.app.bsa.utility.AppConstants;
import com.app.bsa.service.repository.Student;
import com.app.bsa.utility.StudentDataAnalytics;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class StudentsListActivity extends AppCompatActivity{

    private ApplicationViewModel mViewModel;
    private StudentListAdapter mAdapter = null;
    public String mBatchName = "";
    String tSMSNumber;
    ActionMode mCAB = null;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    private StudentsListActivity mContext;
    private static StringBuffer newAdmission = new StringBuffer("\nNew Admission:\n");

    public String selectStudentName ;

    private static BroadcastReceiver smsSentReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_list);


        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext =this;
        //mCAB = mContext.startSupportActionMode(actionModeCallbacks);
        Intent intent = getIntent();
        mBatchName = intent.getExtras().getString(AppConstants.INTENT_KEY.BATCH);
        selectStudentName = intent.getExtras().getString(AppConstants.INTENT_KEY.STUDENT_NAME);


        mViewModel = ViewModelProviders.of(this).get(ApplicationViewModel.class);

        LinearLayout floatingBanner = findViewById(R.id.floating_banner);
        RecyclerView rcyr_students_list = findViewById(R.id.rcyr_students_list);

        if (mAdapter == null) {

            mAdapter = new StudentListAdapter(this, mViewModel);
            mViewModel.getStudentsInBatch(mBatchName).observe(this, vStudentMap -> {
                if(vStudentMap!= null){
                    int presentCnt = StudentDataAnalytics.getCurrMonthActiveStudentCount(vStudentMap);
                    if(presentCnt >= BatchListAdapter.maxStudents) {
                        ColorDrawable colorDrawable
                                = new ColorDrawable(Color.parseColor("#FF0000"));
                            getSupportActionBar().setBackgroundDrawable(colorDrawable);

                    }
                    TextView textView = (TextView) floatingBanner.getChildAt(0);
                    textView.setText(mBatchName + " [ " + presentCnt +" / "+ vStudentMap.size() + " ]");
                    mAdapter.setData(vStudentMap);
                    mRecyclerView.setAdapter(mAdapter);
                    if(selectStudentName!=null)
                      mRecyclerView.scrollToPosition(mAdapter.getSelectedPosition(selectStudentName));
                    mAdapter.notifyDataSetChanged();

                }
            });
        }

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = findViewById(R.id.rcyr_students_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        registerForContextMenu(mRecyclerView);

        FloatingActionButton fab = findViewById(R.id.fab_search);
        fab.setOnClickListener(view -> onClickSearch());





    }

    private void onClickSearch(){
        Intent tSearchIntent = new Intent(this, SearchActivity.class);
        this.startActivity(tSearchIntent);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

    }

    public boolean onPrepareOptionsMenu(Menu menu) {

        (menu.findItem(R.id.canSelection)).setEnabled(true);
        (menu.findItem(R.id.itemfee1)).setEnabled(true);
        (menu.findItem(R.id.itemfee3)).setEnabled(true);
        (menu.findItem(R.id.action_remind)).setEnabled(true);
        (menu.findItem(R.id.action_select_remind)).setVisible(true);
        (menu.findItem(R.id.action_report_attendance)).setEnabled(true);
        (menu.findItem(R.id.action_joining_kit_paid)).setEnabled(true);
        (menu.findItem(R.id.action_remind)).setEnabled(true);

        if (!checkPermissionforSMS()) {
            (menu.findItem(R.id.canSelection)).setEnabled(false);
            (menu.findItem(R.id.itemfee1)).setEnabled(false);
            (menu.findItem(R.id.itemfee3)).setEnabled(false);
            (menu.findItem(R.id.action_remind)).setEnabled(false);
            (menu.findItem(R.id.action_select_remind)).setVisible(false);
            (menu.findItem(R.id.action_report_attendance)).setEnabled(false);
            (menu.findItem(R.id.action_joining_kit_paid)).setEnabled(false);
            (menu.findItem(R.id.action_remind)).setEnabled(false);
        }
        if (mAdapter.ismInSelectionMode()  && !mViewModel.isAdminUser()) {
            (menu.findItem(R.id.action_select_remind)).setVisible(false);
            (menu.findItem(R.id.action_remind)).setEnabled(false);
        } else if (!mAdapter.ismInSelectionMode()) {
            (menu.findItem(R.id.action_remind)).setVisible(false);
            (menu.findItem(R.id.action_select_remind)).setVisible(true);
        };
        if (mAdapter.ismInSelectionMode()) {
            (menu.findItem(R.id.action_remind)).setVisible(true);
            (menu.findItem(R.id.action_select_remind)).setVisible(false);
        };
        ;
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_studentlist_context, menu);
       return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.canSelection:
                mAdapter.getSelectedStudents().clear();
                mAdapter.setmInSelectionMode(false);
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.itemfee1:
                sendAllSMSTextForFee(1, false);
                mAdapter.notifyDataSetChanged();
                break;
            //TODO:: Uncomment if need to add 6 months fee

            case R.id.itemfee6:

                sendAllSMSTextForFee(1, true);
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.itemfee3:
                sendAllSMSTextForFee(3, false);
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.action_report_attendance:
                mAdapter.notifyDataSetChanged();
                if (checkPermissionforSMS()) {
                    sendAttendanceSMS();
                } else {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.err_SMS_permission), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_report_new:
                addStudents();
                mAdapter.notifyDataSetChanged();

                break;

            case R.id.action_joining_kit_paid:

                if (mAdapter.getSelectedStudents().size() == 0) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.err_selection), Toast.LENGTH_SHORT).show();
                    break;
                }
                if (checkPermissionforSMS()) {
                    //Assume need to send SMS as part of updating details
                    updateJoiningKitPaid();
                } else {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.err_SMS_permission), Toast.LENGTH_SHORT).show();
                }
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.action_remind:

                if (!mViewModel.isAdminUser()) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.err_user_not_permitted), Toast.LENGTH_SHORT).show();
                } else {
                    if (checkPermissionforSMS()) {
                        sendReminder();
                        mAdapter.getSelectedStudents().clear();
                        mAdapter.setmInSelectionMode(false);
                    } else {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.err_SMS_permission), Toast.LENGTH_SHORT).show();
                    }
                }
                mAdapter.notifyDataSetChanged();

                break;
            case R.id.action_select_remind:

                if (!mViewModel.isAdminUser()) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.err_user_not_permitted), Toast.LENGTH_SHORT).show();
                } else {
                    if (checkPermissionforSMS()) {
                        mAdapter.setmInSelectionMode(true);

                    } else {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.err_SMS_permission), Toast.LENGTH_SHORT).show();
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.action_settings:
                Toast.makeText(mContext, "Not Implemented", Toast.LENGTH_SHORT).show();
                mAdapter.notifyDataSetChanged();
                break;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;

        }
        return false;
    }


    public void updateJoiningKitPaid() {

        //Validate
        for (String key : mAdapter.getSelectedStudents()) {
            Student tStudent = mViewModel.getStudentById(key).getValue();

            if (tStudent.Joining_fee_paid) {
                //Invalid selection
                Toast.makeText(mContext, mContext.getResources().getString(R.string.err_invalid_sel_joining_fee) + tStudent.getFullName(), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        new android.app.AlertDialog.Builder(mContext)
                .setTitle("Update Joining Kit payment")
                .setMessage("Confirm update?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {

                    StringBuilder sb = new StringBuilder();
                    for (String key : mAdapter.getSelectedStudents() ) {
                        final Student tStudent = mViewModel.getStudentById(key).getValue();

                        if (tStudent == null) {
                            //Error fetching data
                            //USers need to verify after action since there is no feedback currently - async

                        } else {

                            if (tStudent.Joining_fee_paid) {
                                //Already paid - cant update - should not happen
                            } else {
                                //update and send SMS
                                mViewModel.updateJoiningKitPaid(tStudent.Id, true);
                                sb.append(tStudent.First_name).append(",");
                            }
                        }
                    }

                    //Send SMS
                    if (sb.length() > 0) {
                        SmsManager sms = SmsManager.getDefault();

                        String sendsmsText = "Joining Kit fees received from " + sb.toString();
                        try {

                            sms.sendTextMessage(mContext.getResources().getString(R.string.sms_ph_no), null, sendsmsText, null, null);
                        } catch (Exception e) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.err_sms) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();

    }



    private void addStudents() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setTitle("Add Students");
        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> newAdmission.append(input.getText().toString() + "\n"));
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            dialog.dismiss();
        });

        builder.show();
    }

    private void sendReminder() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);

        final HashMap<String, String> tStudentsToSMS = new HashMap<String, String>();
        StringBuilder tSBStudentsWithoutContact = new StringBuilder();

        for (String key : mAdapter.getSelectedStudents()) {


            final Student tStudent = mViewModel.getStudentById(key).getValue();

            if (tStudent == null) {
                //Error fetching data
                tSBStudentsWithoutContact.append(key).append(", ");

            } else {
                if (tStudent.Contact.isEmpty()) {
                    tSBStudentsWithoutContact.append(key).append(", ");
                } else {
                    tStudentsToSMS.put(tStudent.First_name, tStudent.Contact);
                }

            }

        }
        String tTitle = "Reminder To students";
        if (tSBStudentsWithoutContact.length() != 0) {
            tTitle = "Reminder To students" + "\nStudents without contacts are " + tSBStudentsWithoutContact.toString();
        }
        builder.setTitle(tTitle);
        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("REMIND", (dialog, which) -> {
            if (input.getText().toString().equals("Reminder To students")) { //TODO remove
                for (String key : tStudentsToSMS.keySet()) {
                    SmsManager sms = SmsManager.getDefault();

                    String[] paramHolder = {"$kidName"};
                    String[] replaceStr = {key};
                    String sendsmsText = TextUtils.replace(mContext.getString(R.string.key_reminder_msg), paramHolder, replaceStr).toString();
                    int tCount = sendsmsText.length();
                    try {

                        sms.sendTextMessage(tStudentsToSMS.get(key), null, sendsmsText, null, null);

                        final ProgressDialog dialog1 = new ProgressDialog(mContext.getApplicationContext());
                        dialog1.setTitle("Sending SMS..." + tCount);
                        dialog1.setMessage("Please wait.");
                        dialog1.setIndeterminate(true);
                        dialog1.setCancelable(false);
                        dialog1.show();

                        long delayInMillis = 1500;
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                dialog1.dismiss();
                            }
                        }, delayInMillis);
                        mCAB.invalidate();

                    } catch (Exception e) {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.error) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
            tStudentsToSMS.clear();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            dialog.dismiss();
        });

        builder.show();
    }

    private void updateAttendance(ArrayList<String> vStudentIdList) {

        Date tToday = new Date();
        //Switch to JAva 8 eventually with LocalDate - not doing it now as needs API to be 26 and above.
        //Not sure everyones phone will support.
        boolean bRefresh = false;

        int counter = vStudentIdList.size();
        for (String key : vStudentIdList) {
            counter--;
            if (counter == 0) {
                bRefresh = true;
            }

            Calendar c1 = Calendar.getInstance();
            c1.setTime(tToday);

            Calendar c2 = Calendar.getInstance();
            c2.setTime(mViewModel.getStudentById(key).getValue().Last_attended);

            int mnth_att_cnt = mViewModel.getStudentById(key).getValue().Monthly_attendance_count;

            if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                    && c1.get(Calendar.YEAR) == c1.get(Calendar.YEAR)) {
                mnth_att_cnt++;
            } else {
                mnth_att_cnt = 1;
            }

            HashMap<String, Object> studentAttendance = new HashMap();
            studentAttendance.put(DBConstants.STUDENT.LAST_ATTENDED, tToday);
            studentAttendance.put(DBConstants.STUDENT.MONTHLY_ATTENDANCE_COUNT, mnth_att_cnt);

            mViewModel.updateAttendance(key, studentAttendance, bRefresh);
        }

    }


    private void sendAttendanceSMS() {



        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        final StringBuilder tSB = new StringBuilder("App Verion : " + mContext.getResources().getString(R.string.app_version) + "\n\n");

        tSB.append("Students For Batch :" + mBatchName + "\n");
        for (String key : mAdapter.getSelectedStudents()) {
            tSB.append((mViewModel.getStudentById(key).getValue()).getFullName() + "\n");
        }

        ArrayList<String> tSelectedStudents = new ArrayList(mAdapter.getSelectedStudents());

        builder.setMessage(tSB.append(newAdmission));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                smsSentReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                updateAttendance(mAdapter.getSelectedStudents());
                                Toast.makeText(mContext.getApplicationContext(), mContext.getResources().getString(R.string.sms_msg_success), Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                Toast.makeText(context,
                                        "Failed to Send message :: GENERIC ERROR",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                Toast.makeText(context,
                                        "Failed to Send message :: NO_SERVICE",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                Toast.makeText(context,
                                        "Failed to Send message :: NULL PDU",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                Toast.makeText(context,
                                        "Failed to Send message :: NETWORK NOT FOUND",
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                        mContext.unregisterReceiver(smsSentReceiver);
                    }
                };

                mContext.registerReceiver(smsSentReceiver, new IntentFilter("SMS_SENT"));
                SmsManager sms = SmsManager.getDefault();
                ArrayList<String> parts = sms.divideMessage(tSB.toString());
                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                for (int i = 0; i < parts.size(); i++) {
                    sentIntents.add(PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, new Intent(
                            "SMS_SENT"), 0));
                }

                tSMSNumber = tSMSNumber == null ? mContext.getResources().getString(R.string.sms_ph_no) : tSMSNumber;
                sms.sendMultipartTextMessage(tSMSNumber, null, parts, sentIntents, null);

                newAdmission = new StringBuffer("\nNEW Admission:\n");
            }
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
            //Nothing to do
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void sendAllSMSTextForFee(final int vPeriod, boolean isHalfMonth) {

        if (checkPermissionforSMS()) {
            for (String tStudentId :mAdapter.getSelectedStudents()) {
                sendSMSTextForFeeAndUpdateStudentStatus(vPeriod, tStudentId, isHalfMonth);

            }
        }
    }


    private void sendSMSTextForFeeAndUpdateStudentStatus(final int vPeriod, String vStudentId, boolean isHalfMonth) {

        final Student tStudent = mViewModel.getStudentById(vStudentId).getValue();
        String tContact = "";
        String tSMSTextString = "";

        if (tStudent == null) {
            //Error fetching data
            Toast.makeText(mContext, "Could not find data. Contact details not available", Toast.LENGTH_SHORT).show();
            return;
        } else {
            tContact = tStudent.Contact;
            if (tContact.isEmpty()) {
                //No point proceeding
                Toast.makeText(mContext, "Contact details not available", Toast.LENGTH_SHORT).show();
                return;
            }

            String tName = tStudent.getFullName();

            String[] replaceStr = {tName, tStudent.Batch_name, isHalfMonth ? vPeriod + "/2" : vPeriod + ""};
            String[] paramHolder = {"$lstName", "$batchStr", "$period"};
            tSMSTextString = TextUtils.replace(mContext.getResources().getString(R.string.msg_fee_rcvd), paramHolder, replaceStr).toString();

        }

        tSMSNumber = tSMSNumber == null ? mContext.getResources().getString(R.string.sms_ph_no) : tSMSNumber;

        //// Hacky code need to think more but later
        tStudent.setAdmin(mViewModel.isAdminUser());
        if (mViewModel.isAdminUser()) {
            //Only allow actual sending if user is an admin
            tSMSNumber = tContact;
        }

        final String SMSNumber = tSMSNumber;
        final String tSMSText = tSMSTextString;
        final boolean tUpdateFees = mViewModel.isAdminUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(Html.fromHtml(tSMSText));
        builder.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(SMSNumber, null, tSMSText, null, null);

            if (tUpdateFees) {
                //Update fees only after admin sends the actual SMS to parents.
                updateFeeStatus(tStudent, vPeriod, isHalfMonth);
                Toast.makeText(mContext, mContext.getResources().getString(R.string.msg_subscription_extended), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {

        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void updateFeeStatus(Student vStudent, int vExtension, boolean isHalfMonth) {

        try {

            //String tName = vStudent.getFullName();
            int tMonthMM = Integer.parseInt(vStudent.Fee_status.substring(0, 2));
            int tYearYYYY = Integer.parseInt(vStudent.Fee_status.substring(3, 7));

            if ((tMonthMM + vExtension) > 12) {

                tMonthMM = (tMonthMM + vExtension) % 12;
                tYearYYYY++;

            } else {

                tMonthMM = tMonthMM + vExtension;
            }
            String tNewFeeValue = String.format("%02d/%04d", tMonthMM, tYearYYYY);

            mViewModel.updateFeeStatus(vStudent.Id, tNewFeeValue, isHalfMonth);

        } catch (NumberFormatException e) {
            //No action to be taken currently
            //users to verify if updates are done
            Toast.makeText(mContext, "Something went wrong - please verify before retrying!", Toast.LENGTH_SHORT).show();
        }


    }


    public boolean checkPermissionforSMS() {

        boolean tReturn = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                String[] permissions = {Manifest.permission.SEND_SMS};
                mContext.requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(mContext,
                    Manifest.permission.SEND_SMS)) {

            } else {
                // No explanation needed, we can request the permission
                ActivityCompat.requestPermissions(mContext,
                        new String[]{Manifest.permission.SEND_SMS},
                        PERMISSION_REQUEST_CODE);

            }
        } else {
            // Permission has already been granted
            tReturn = true;
        }

        return tReturn;
    }

}
