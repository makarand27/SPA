package com.app.spa;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.app.spa.service.repository.Student;
import com.app.spa.utility.AccountSummary;
import com.app.spa.utility.AppConstants;
import com.app.spa.utility.StudentDataAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class StudentAnalyticsActivity extends AppCompatActivity {
    private ApplicationViewModel mViewModel;
    WebView simpleWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ViewGroup mContainerView = findViewById(R.id.layoutWebV);
            LayoutTransition lt = new LayoutTransition();
            lt.disableTransitionType(LayoutTransition.DISAPPEARING);
            lt.enableTransitionType(LayoutTransition.CHANGING);
            mContainerView.setLayoutTransition(lt);
        }
        mViewModel = ViewModelProviders.of(this).get(ApplicationViewModel.class);
        simpleWebView= findViewById(R.id.simpleWebView);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String action = getIntent().getStringExtra(AppConstants.INTENT_KEY.ACTION);
        if (AppConstants.INTENT_VALUES.SUMMARY_LEVELWISE_ACTION.equals(action)) {
            showLevelWiseStudentData();
        } else if (AppConstants.INTENT_VALUES.SUMMARY_ACTION.equals(action)) {
            showSummaryStudentData();
        } else if (AppConstants.INTENT_VALUES.BATCH_WISE_SUMMARY.equals(action)) {
            showBatchWiseAccountSummary();
            //showSummaryStudentData();
        }

    }


    private void showBatchWiseAccountSummary() {


        HashMap<String, Student> tStudentMap = mViewModel.getAllStudentData();
        HashMap<String,Double> tFeeMap = mViewModel.getFeeData();
        HashMap<String, Student> newStdMap = new HashMap<>();
        ArrayList<String> batchWiseLst= getIntent().getStringArrayListExtra(AppConstants.INTENT_KEY.BATCH_WISE_SUMMARY);

        Set keySet = tStudentMap.keySet();
            Iterator keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                String key = (String) keyIterator.next();
                Student tStudent = tStudentMap.get(key);
                if(batchWiseLst.contains(tStudent.Batch_name)){
                    newStdMap.put(key,tStudent);
                }

            }

            AccountSummary tAccSumm =  new AccountSummary(newStdMap,tFeeMap);
             String summaryText = tAccSumm.getAccountSummary();
            simpleWebView.loadData(summaryText, "text/html", "UTF-8");

    }
    private void showSummaryStudentData() {

        HashMap<String, Student> tStudentMap = mViewModel.getAllStudentData();
        HashMap<String,Double> tFeeMap = mViewModel.getFeeData();
        AccountSummary tAccSumm =  new AccountSummary(tStudentMap, tFeeMap);
        String summaryText = tAccSumm.getAccountSummary();
        simpleWebView.loadData(summaryText, "text/html", "UTF-8");

    }
    private void showLevelWiseStudentData() {

        if(!mViewModel.isAdminUser()){
            Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, Student> tStudentMap = mViewModel.getAllStudentData();
        StudentDataAnalytics tLevelSumm =  new StudentDataAnalytics(tStudentMap);
        String summaryText = tLevelSumm.calcLevelwiseStudentCount();
        // load static html data on a web view
        simpleWebView.loadData(summaryText, "text/html", "UTF-8");


    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0,R.anim.slide_out_up);
                return true;
        }
        return false;
    }
}
