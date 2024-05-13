package com.app.bsa;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.MultiSelectListPreference;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.androidbuts.multispinnerfilter.SingleSpinnerListener;
import com.app.bsa.service.firestore.BatchDao;
import com.app.bsa.service.repository.Student;
import com.app.bsa.utility.AppConstants;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BatchMaintenanceActivity extends AppCompatActivity {
    private ApplicationViewModel mViewModel;
    ArrayAdapter<CharSequence> mBatchListAdapter;
    ArrayAdapter<CharSequence> mCoachListAdapter;
    AutoCompleteTextView mBatchSpinner;
    AutoCompleteTextView mCoachSpinner;
    TextInputEditText mCoachLbl;
    String batchId;

    AutoCompleteTextView lst_coach;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_batchdetails_context, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;

            case R.id.ab_btn_update_batch:
                onClickUpdate();
                return true;
        }
        return false;

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;

    }
    private void onClickUpdate() {

        new AlertDialog.Builder(this)
                .setTitle("Update Batch")
                .setMessage("Confirm update?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    BatchDao bo = new BatchDao();
                    bo.batch_name=  mBatchSpinner.getText().toString().trim();
                    bo.id=batchId;
                    if(bo.id!=null && ! bo.id.isEmpty())
                        mViewModel.deleteBatchPermissionData(bo);
                    bo.permission = mCoachSpinner.getText().toString().trim();
                    if(!mCoachLbl.getText().toString().isEmpty() && !bo.permission.isEmpty())
                        bo.permission += ","+mCoachLbl.getText().toString().trim();
                    else if(!mCoachLbl.getText().toString().isEmpty() && bo.permission.isEmpty())
                        bo.permission += mCoachLbl.getText().toString().trim();

                    mViewModel.updateBatchPermissionData(bo);
                   finish();

                })
                .setNegativeButton(android.R.string.no, null).show();

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_maintenance);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ViewGroup mContainerView = findViewById(R.id.batchMaintenance);
            LayoutTransition lt = new LayoutTransition();
            lt.disableTransitionType(LayoutTransition.DISAPPEARING);
            lt.enableTransitionType(LayoutTransition.CHANGING);
            mContainerView.setLayoutTransition(lt);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mViewModel = ViewModelProviders.of(this).get(ApplicationViewModel.class);

            mBatchSpinner = findViewById(R.id.txt_batch);
            mCoachSpinner = findViewById(R.id.txt_coach);
            mCoachLbl = findViewById(R.id.txt_coach_name);
            //Populate level dropdown

            ArrayList<String> lBatchList = mViewModel.getBatchList().getValue();
            Collections.sort(lBatchList);
            List<CharSequence> tSortedBatchList = new ArrayList(lBatchList);

            mBatchListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tSortedBatchList);
            mBatchListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBatchSpinner.setAdapter(mBatchListAdapter);

            final HashMap<String,BatchDao> tbatchPermissionLst = mViewModel.getBatchPermissionData();
            String batchName = getIntent().getStringExtra(AppConstants.INTENT_KEY.BATCH);
            mBatchSpinner.setText(batchName);
            BatchDao tCoachNames =tbatchPermissionLst.get(batchName);
            if (tCoachNames != null) {
                mCoachLbl.setText( tCoachNames.permission);
                batchId =tCoachNames.id;
            }else{
                mCoachLbl.setText(null);
            }
//            mBatchSpinner.setOnItemClickListener((parentView, view, position, id) -> {
//                BatchDao tCoachNames =tbatchPermissionLst.get(mBatchSpinner.getText().toString());
//                if (tCoachNames != null) {
//                    mCoachLbl.setText( tCoachNames.permission);
//                    batchId =tCoachNames.id;
//                }else{
//                    mCoachLbl.setText(null);
//                }
//            });

            // ArrayList<String> tBatchPerm = new ArrayList(tbatchPermissionLst.keySet());
            ArrayList<String> tCoachLst = new ArrayList(mViewModel.getCoachData().keySet());
            Collections.sort(tCoachLst);
            List<CharSequence> tSortedBList = new ArrayList(tCoachLst);

            mCoachListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tSortedBList);
            mCoachListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCoachSpinner.setAdapter(mCoachListAdapter);

            mCoachSpinner.setOnItemClickListener((parentView, view, position, id) -> {

                mCoachLbl.setText(mCoachLbl.getText().toString() +","+  mCoachSpinner.getText());

            });
        }
    }
}
