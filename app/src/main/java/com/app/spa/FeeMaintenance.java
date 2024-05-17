package com.app.spa;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



public class FeeMaintenance extends AppCompatActivity {
    private ApplicationViewModel mViewModel;
    AutoCompleteTextView mLevelSpinner;

    ImageButton mAddButton;
    ImageButton mUpdateButton;
    ImageButton mDeleteButton;

    TextInputEditText mFeeEdit;
    TextInputEditText mFeeAdd;
    TextInputEditText mLevelAdd;
    String mSelectedLevel="";

    ArrayAdapter<CharSequence> mLevelListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_maintenance);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ViewGroup mContainerView = findViewById(R.id.feeMaintenance);
            LayoutTransition lt = new LayoutTransition();
            lt.disableTransitionType(LayoutTransition.DISAPPEARING);
            lt.enableTransitionType(LayoutTransition.CHANGING);
            mContainerView.setLayoutTransition(lt);
          
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAddButton = findViewById(R.id.btn_fee_add);
        mDeleteButton = findViewById(R.id.btn_fee_delete);
        mUpdateButton = findViewById(R.id.btn_fee_update);
        mFeeAdd = findViewById(R.id.txt_fee_value_add);
        mFeeEdit = findViewById(R.id.txt_fee_edit);
        mLevelAdd = findViewById(R.id.txt_fee_name_add);
        mLevelSpinner = findViewById(R.id.sp_fees);


        mViewModel = ViewModelProviders.of(this).get(ApplicationViewModel.class);


        //Populate level dropdown
        final HashMap<String, Double> tFeeMap = mViewModel.getFeeData();
        ArrayList<String> tFeeLevelNames = new ArrayList(tFeeMap.keySet());
        Collections.sort(tFeeLevelNames);
        List<CharSequence> tSortedList = new ArrayList(tFeeLevelNames);

        mLevelListAdapter = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_spinner_item,tSortedList);
        mLevelListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLevelSpinner.setAdapter(mLevelListAdapter);
        if(tSortedList.size() > 0 ) mLevelSpinner.setText(tSortedList.get(0).toString(),false);

       mLevelSpinner.setOnItemClickListener((parentView, view, position, id) -> {
           Double tFeeAmount = tFeeMap.get(mLevelSpinner.getText().toString());
           if (tFeeAmount != null) {
               mFeeEdit.setText(String.format("%.0f", tFeeAmount));
               mSelectedLevel = mLevelSpinner.getText().toString();
           }
       });
        mLevelSpinner.setSelection(0);

        setListenerActions();
    }



    private void onClickDelete() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Fee")
                .setMessage("Confirm Delete?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    mViewModel.deleteFee(mLevelSpinner.getText().toString());
                    //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    finish();
                })
                .setNegativeButton(android.R.string.no, null).show();

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;

    }
    private void onClickAdd() {


        try{
            final Double tFeeAmount = Double.parseDouble(mFeeAdd.getText().toString());

            new AlertDialog.Builder(this)
                    .setTitle("Add Fee")
                    .setMessage("Confirm Addition?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        mViewModel.addFee(mLevelAdd.getText().toString(),tFeeAmount);
                        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        finish();
                    })
                    .setNegativeButton(android.R.string.no, null).show();

        }catch (NumberFormatException e){

            Toast.makeText(this, getResources().getString(R.string.err_number_format), Toast.LENGTH_SHORT).show();
        }


    }
    private void onClickUpdate() {

        try{
            final Double tFeeAmount = Double.parseDouble(mFeeEdit.getText().toString());

            new AlertDialog.Builder(this)
                    .setTitle("Update Fee")
                    .setMessage("Confirm Update?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        mViewModel.updateFee(mLevelSpinner.getText().toString(),tFeeAmount);
                        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        finish();
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }catch(NumberFormatException e){
            Toast.makeText(this, getResources().getString(R.string.err_number_format), Toast.LENGTH_SHORT).show();
        }

    }
    private void setListenerActions() {

        mAddButton.setOnClickListener(view -> onClickAdd());

        mDeleteButton.setOnClickListener(view -> onClickDelete());

        mUpdateButton.setOnClickListener(view -> onClickUpdate());

    }

}
