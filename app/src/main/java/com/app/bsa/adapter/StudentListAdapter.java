package com.app.bsa.adapter;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
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
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bsa.ApplicationViewModel;
import com.app.bsa.PreferenceActivity;
import com.app.bsa.R;
import com.app.bsa.StudentMaintActivity;
import com.app.bsa.StudentsListActivity;
import com.app.bsa.service.firestore.DBConstants;
import com.app.bsa.utility.AppConstants;
import com.app.bsa.service.repository.Student;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.app.bsa.utility.AppConstants.PERMISSION_REQUEST_CODE;

import lombok.extern.log4j.Log4j2;

public class StudentListAdapter extends RecyclerView.Adapter {

    static SimpleDateFormat mSdf_dd = new SimpleDateFormat("dd");
    static SimpleDateFormat mSdf_mmm = new SimpleDateFormat("MMM");

    private HashMap<String, Student> mDataMap = new HashMap<String, Student>();
    private ArrayList<String> mDataset = new ArrayList<>();
    private ArrayList<String> mSelectedStudents = new ArrayList<>();
    private ApplicationViewModel mViewModel;
    private boolean[] mChecked;

    public boolean ismInSelectionMode() {
        return mInSelectionMode;
    }

    public void setmInSelectionMode(boolean mInSelectionMode) {
        this.mInSelectionMode = mInSelectionMode;
    }

    boolean mInSelectionMode = false;
    ActionMode mCAB = null;
    private StudentsListActivity mContext;
    int mCurMM = 0;
    int mCurYYYY = 0;

    private static StringBuffer newAdmission = new StringBuffer("\nNew Admission:\n");

    public StudentListAdapter(Context vContext, ApplicationViewModel vVModel) {
        mContext = (StudentsListActivity) vContext;
        mViewModel = vVModel;
    }

    public ArrayList<String> getSelectedStudents() {
        return mSelectedStudents;
    }

    public void setData(HashMap<String, Student> vStudentMap) {
        mDataset.clear();
        mDataMap.clear();
        mDataMap.putAll(vStudentMap);
        mDataset.addAll(vStudentMap.keySet());
        mChecked = new boolean[mDataset.size()];
        mSelectedStudents.clear();

        Collections.sort(mDataset);

        Calendar cal1 = Calendar.getInstance();
        mCurMM = cal1.get(Calendar.MONTH) + 1;
        mCurYYYY = cal1.get(Calendar.YEAR);
    }

    public int getSelectedPosition(String stdId){

        for(int i= 0; i <mDataset.size();i++){
            String sName =mDataset.get(i);
            if(sName.contains(stdId)){
                return i;
            }

        }
        return 0;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mDataset.size() > 0) {
            ((SimpleViewHolder) holder).bindData(mDataset.get(position),mContext.selectStudentName);
        }
    }

    public void selectItem( String vStudentId) {
        mSelectedStudents.add(vStudentId);
    }

    public void deSelectItem( String vStudentId) {
        mSelectedStudents.remove(vStudentId);
        if (mSelectedStudents.size() == 0) {
            resetAllSelections();
        }
    }

    public void resetAllSelections() {

        if (mCAB != null) {
            mCAB.finish();
        }
    }

    public boolean isSelected(String studentId) {
        return mSelectedStudents.contains(studentId);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.list_checkbox_item;
    }


    public void updateJoiningKitPaid() {

        //Validate
        for (String key : mSelectedStudents) {
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
                    for (String key : mSelectedStudents) {
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


    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        private MaterialTextView NameTextView;
        private MaterialTextView LevelTextView;
        private MaterialTextView AttendanceTextView;
        private ImageView mCheckImage;
        String tThisMonth = mSdf_mmm.format(new Date());


        public SimpleViewHolder(final View itemView) {
            super(itemView);
            NameTextView = itemView.findViewById(R.id.name);
            LevelTextView = itemView.findViewById(R.id.level);
            AttendanceTextView = itemView.findViewById(R.id.last_attended);
            mCheckImage = itemView.findViewById(R.id.img_check);
            NameTextView.setBackgroundColor(Color.TRANSPARENT);
            LevelTextView.setBackgroundColor(Color.TRANSPARENT);
            AttendanceTextView.setBackgroundColor(Color.TRANSPARENT);
            mCheckImage.setBackgroundColor(Color.TRANSPARENT);
        }


        public void bindData(final String vName,final String selectedStdId) {


            if (mDataset.size() > 0) {

                this.itemView.setBackgroundColor(Color.TRANSPARENT);
                NameTextView.setTextColor(Color.BLACK);
                LevelTextView.setTextColor(Color.BLACK);

                if (Student.isFeeOverdue(mDataMap.get(vName), mCurMM, mCurYYYY)) {

                    NameTextView.setTextColor(Color.RED);
                    LevelTextView.setTextColor(Color.RED);
                    mDataMap.get(vName).fee_overdue = true;

                    if (mInSelectionMode && mDataMap.get(vName).Monthly_attendance_count >= 1
                            && mDataMap.get(vName).isPresentCurrentMnth()) {

                            selectItem(mDataMap.get(vName).Id);

                    }


                }

                LevelTextView.setText(mDataMap.get(vName).Level);
                NameTextView.setText(mDataMap.get(vName).getFullName());

                String tMonth = mDataMap.get(vName).Last_attended == null ? "Absent" : mSdf_mmm.format(mDataMap.get(vName).Last_attended);
                String tDay = mDataMap.get(vName).Last_attended == null ? "" : mSdf_dd.format(mDataMap.get(vName).Last_attended);
                //Not handling year old deactivated for performance reasons - only checking month - if there is business need then can be added in


                String attendanceTV = tMonth + " " + tDay;
                if(mDataMap.get(vName).Monthly_attendance_count>0) {
                    attendanceTV += "(" + mDataMap.get(vName).Monthly_attendance_count+")";
                }

                AttendanceTextView.setText(attendanceTV);

                if (tThisMonth.equals(tMonth)) {
                    AttendanceTextView.setTextColor(Color.BLUE);
                } else {
                    AttendanceTextView.setTextColor(Color.RED);
                }

                if (isSelected(mDataMap.get(vName).Id)) {
                    this.itemView.setBackgroundColor(Color.parseColor("#ADD8E6"));
                } else {
                    this.itemView.setBackgroundColor(Color.TRANSPARENT);
                }

                if(mDataMap.get(vName).getFullName().equalsIgnoreCase(selectedStdId)){

                    SpannableStringBuilder builder = new SpannableStringBuilder(NameTextView.getText());
                    int startIndex = 0;
                    int endIndex = NameTextView.length();
                    builder.setSpan(new BackgroundColorSpan(Color.YELLOW), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    NameTextView.setText(builder);

                }

                this.itemView.setOnClickListener(v -> {

                    if (!isSelected(mDataMap.get(vName).Id)) {
                            //select item
                        selectItem(mDataMap.get(vName).Id);
                        v.setBackgroundColor(Color.parseColor("#ADD8E6"));
                } else {
                        deSelectItem(mDataMap.get(vName).Id);
                        v.setBackgroundColor(Color.TRANSPARENT);
                    }

                });

                this.itemView.setOnLongClickListener(v -> {
                    if (!isSelected(mDataMap.get(vName).Id)) {
                        Intent tMaintIntent = new Intent(mContext, StudentMaintActivity.class);
                        tMaintIntent.putExtra(AppConstants.INTENT_KEY.ACTION, AppConstants.INTENT_VALUES.MAINT_ACTION_VIEW);
                        tMaintIntent.putExtra(AppConstants.INTENT_KEY.STUDENT_ID, mDataMap.get(vName).Id);
                        mContext.startActivity(tMaintIntent);
                        mContext.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    }
                    return true;
                });
            }

        }

    }


    private int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio;
        float g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio;
        float b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio;
        return Color.rgb((int) r, (int) g, (int) b);
    }

    //CAB

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
