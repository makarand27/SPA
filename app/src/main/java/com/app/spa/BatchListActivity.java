package com.app.spa;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.app.spa.adapter.BatchListAdapter;
import com.app.spa.service.firestore.FirestoreData;
import com.app.spa.utility.AccountSummary;
import com.app.spa.utility.AppConstants;
import com.app.spa.service.repository.Student;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class BatchListActivity extends AppCompatActivity {

    private ApplicationViewModel mViewModel;

    private BatchListAdapter mAdapter = null;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    MaterialTextView mPendingStu;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProgressDialog p = new ProgressDialog(this);
        p.setMessage("Loading ...");
        p.setCancelable(false);
        p.show();
        setContentView(R.layout.activity_batch_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if(FirestoreData.isDev){
            toolbar.setBackgroundColor(Color.RED);
            toolbar.setTitle("DEV......");
        }
        setSupportActionBar(toolbar);

        mViewModel = ViewModelProviders.of(this).get(ApplicationViewModel.class);

        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);

        mSwipeRefreshLayout.setOnRefreshListener(() -> onBatchRefresh());

        if(mAdapter==null){

            mAdapter = new BatchListAdapter(this, mViewModel,this);

            mViewModel.getBatchListToStdCnt().observe(this, vBatchNames -> {
                if(vBatchNames!= null && !vBatchNames.isEmpty()) {

                mAdapter.setData(vBatchNames);
                mAdapter.notifyDataSetChanged();
                p.hide();
                p.dismiss();
                if(mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            } else {
                    p.hide();
                    p.dismiss();
                    if(mSwipeRefreshLayout.isRefreshing()){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    return;
                }
            });
            mViewModel.getCurrentDataSource().observe(this, vCurrDataSource -> mAdapter.setCurrentDataSource(vCurrDataSource));
        }
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = findViewById(R.id.rcyr_batch_list);
        mRecyclerView.setBackgroundColor(22);
        mRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setAdapter(mAdapter);
        mPendingStu = findViewById(R.id.pendingStu);
        FloatingActionButton fab = findViewById(R.id.fab_search);
        fab.setOnClickListener(view -> onClickSearch());

    }


    public void onBatchRefresh() {
        // This method performs the actual data-refresh operation.
        // The method calls setRefreshing(false) when it's finished.
        mViewModel.reloadData();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        (menu.findItem(R.id.action_export)).setVisible(mViewModel.isAdminUser());
        (menu.findItem(R.id.action_add_student)).setVisible(mViewModel.isAdminUser());
        (menu.findItem(R.id.action_batch)).setVisible(mViewModel.isAdminUser());
        (menu.findItem(R.id.action_settings)).setVisible(mViewModel.isAdminUser());
        (menu.findItem(R.id.action_account_summary)).setVisible(mViewModel.isAdminUser());
        (menu.findItem(R.id.action_levelwise_summary)).setVisible(mViewModel.isAdminUser());
        (menu.findItem(R.id.action_fees)).setVisible(mViewModel.isAdminUser());


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;

    }

    private void onClickSearch(){
        Intent tSearchIntent = new Intent(this, SearchActivity.class);
        this.startActivity(tSearchIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_sign_out :
                new AlertDialog.Builder(this)
                        .setTitle("Signing out...")
                        .setMessage("Are you sure you want to sign out?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            mViewModel.signOut();
                            startActivity(new Intent(BatchListActivity.this, SignInActivity.class));
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        })
                        .setNegativeButton(android.R.string.no, null).show();

                return true;

            case R.id.action_settings :

                startActivity(new Intent(BatchListActivity.this, PreferenceActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;

            case R.id.swiperefresh :
                onBatchRefresh();
                return true;

            case R.id.action_account_summary :

                if(!mViewModel.isAdminUser()){
                    Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
                } else{

                    Intent tStudentAnalytics = new Intent(this, StudentAnalyticsActivity.class);
                    if(mAdapter.getmSelectedBatches().size()>0 ){
                        tStudentAnalytics.putExtra(AppConstants.INTENT_KEY.ACTION,AppConstants.INTENT_VALUES.BATCH_WISE_SUMMARY);
                         tStudentAnalytics.putStringArrayListExtra(AppConstants.INTENT_KEY.BATCH_WISE_SUMMARY,mAdapter.getmSelectedBatches());

                    }else {
                        tStudentAnalytics.putExtra(AppConstants.INTENT_KEY.ACTION,AppConstants.INTENT_VALUES.SUMMARY_ACTION);

                    }
                    this.startActivity(tStudentAnalytics);

                    overridePendingTransition(R.anim.slide_in_up, 0);
                }

                return true;

            case R.id.action_levelwise_summary:

                if(!mViewModel.isAdminUser()){
                    Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
                } else{
                    Intent tStudentAnalytics = new Intent(this, StudentAnalyticsActivity.class);
                    tStudentAnalytics.putExtra(AppConstants.INTENT_KEY.ACTION,AppConstants.INTENT_VALUES.SUMMARY_LEVELWISE_ACTION);
                    this.startActivity(tStudentAnalytics);
                    overridePendingTransition(R.anim.slide_in_up, 0);
                }

                return true;

            case R.id.action_batch:
                if(!mViewModel.isAdminUser()){
                    Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
                } else{
                    Intent tMaintIntent = new Intent(this, BatchMaintenanceActivity.class);
                    this.startActivity(tMaintIntent);
                    overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left);

                }
                return true;

            case R.id.action_fees:

                if(!mViewModel.isAdminUser()){
                    Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
                } else{
                    Intent tMaintIntent = new Intent(this, FeeMaintenance.class);
                    tMaintIntent.putExtra(AppConstants.INTENT_KEY.ACTION, AppConstants.INTENT_VALUES.MAINT_ACTION_ADD);
                    this.startActivity(tMaintIntent);
                    overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left);

                }

                return true;
            case R.id.fab_search:

                Intent tSearchIntent = new Intent(this, SearchActivity.class);
                this.startActivity(tSearchIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                return true;
            case R.id.action_add_student :

                if(!mViewModel.isAdminUser()){
                    Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
                } else{
                    Intent tMaintIntent = new Intent(this, StudentMaintActivity.class);
                    tMaintIntent.putExtra(AppConstants.INTENT_KEY.ACTION, AppConstants.INTENT_VALUES.MAINT_ACTION_ADD);

                    this.startActivity(tMaintIntent);
                    overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left);

                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                }

                /** whats app code need to enable at some time
                 if(true){

                 Intent intent = new Intent(Intent.ACTION_SEND);

                 intent.setType("text/plain");
                 intent.setPackage("com.whatsapp");

                 // Give your message here
                 intent.putExtra(Intent.EXTRA_TEXT,"hello");

                 // Checking whether Whatsapp
                 // is installed or not
                 if (intent.resolveActivity(getPackageManager())== null) {
                 Toast.makeText(this,"Please install whatsapp first.",Toast.LENGTH_SHORT).show();
                 return true;
                 }
                 // Starting Whatsapp
                 startActivity(intent);
                 }
                 */

                return true;
            case R.id.action_import :
                if(!mViewModel.isAdminUser() || ! FirestoreData.isDev){
                    Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
                } else if(FirestoreData.loadProdData) {
                    importAllStudentsToDev();
                }
                return true;
            case R.id.action_export :

                if(!mViewModel.isAdminUser()){
                    Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
                } else{

                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        if(!checkFilePermission()) {
                            requestFilePermission();
                        }
                        else{
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd-HH-mm-ss");
                            Date tDate = new Date();
                            String tFileName = "SPA_export_"+ formatter.format(tDate)+".txt";
                            if(export(tFileName)){
                                Toast.makeText(this, "File exported as : " + tFileName, Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(this, "Something went wrong - failed to export", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }

    }


    public static boolean sendWhatAppMessages(String message, String phone, Context context){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + message;
        sendIntent.setData(Uri.parse(url));
        context.startActivity(sendIntent);
        return true;
    }
    private boolean checkFilePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestFilePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Access is required for creating data export file. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, AppConstants.PERMISSION_REQUEST_CODE);
        }
    }

    private void showLevelWiseStudentData() {

        if(!mViewModel.isAdminUser()){
            Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
            return;
        }
        WebView simpleWebView= findViewById(R.id.simpleWebView);

        String customHtml = "<html><body><h1>Hello, AbhiAndroid</h1>" +
                "<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>" +
                "<p>This is a sample paragraph of static HTML In Web view</p>" +
                "</body></html>";
// load static html data on a web view
        simpleWebView.loadData(customHtml, "text/html", "UTF-8");
    }
    private void showAccountSummary() {

        if(!mViewModel.isAdminUser()){
            Toast.makeText(this, "Permission not available", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Student> tStudentMap = mViewModel.getAllStudentData();
        HashMap<String,Double> tFeeMap = mViewModel.getFeeData();

        AccountSummary tAccSumm =  new AccountSummary(tStudentMap, tFeeMap);
        String summaryText = tAccSumm.getAccountSummary();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            builder.setMessage(HtmlCompat.fromHtml(summaryText,HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

        builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    public void importAllStudentsToDev(){
        FirestoreData.getInstance().LoadStudentDataFromProd(false);
    }

    public boolean export(String vFileName){

        HashMap<String,Student> tStudentMap = mViewModel.getAllStudentData();
        Iterator keyIterator = tStudentMap.keySet().iterator();
        boolean tExportDone = false;

        if(tStudentMap != null) {
            try {
                File tExportFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), vFileName);
                FileOutputStream fos = new FileOutputStream(tExportFile);
                while (keyIterator.hasNext()) {
                    Student tStudent = tStudentMap.get((String)keyIterator.next());
                    String tExportLine = tStudent.getExportString()+"\n";
                    fos.write(tExportLine.getBytes());

                }
                fos.close();
                tExportDone = true;

            } catch (IOException e) {
                //TODO - error handling
                e.printStackTrace();
            }
        }
        return tExportDone;

    }

}
