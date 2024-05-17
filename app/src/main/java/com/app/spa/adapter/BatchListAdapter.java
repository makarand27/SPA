package com.app.spa.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.spa.App;
import com.app.spa.ApplicationViewModel;
import com.app.spa.BatchMaintenanceActivity;
import com.app.spa.R;
import com.app.spa.StudentsListActivity;
import com.app.spa.service.firestore.DBConstants;
import com.app.spa.service.firestore.FirestoreData;
import com.app.spa.utility.AppConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BatchListAdapter extends RecyclerView.Adapter {


    private ArrayList<String> mDataset = new ArrayList();
    private Map<String,String> mBatch2StdCnt = new HashMap<>();
    Context mContext;
    private ApplicationViewModel mViewModel;
    private String mCurrentDataSource =  DBConstants.SOURCE_NOTSET;
    Activity batchListActivity;

    public ArrayList<String> getmSelectedBatches() {
        return mSelectedBatches;
    }

    private ArrayList<String> mSelectedBatches = new ArrayList<>();
    public static Integer maxStudents;
    static {
        maxStudents = Integer.parseInt(App.getRes().getString(R.string.max_stu_in_batch));
    }
    public BatchListAdapter(Context vContext, ApplicationViewModel vViewModel,Activity batchLstActivity){
        mContext= vContext;
        mViewModel = vViewModel;
        batchListActivity = batchLstActivity;

    }
    public void setCurrentDataSource(String vCurrentDataSource){
        mCurrentDataSource = vCurrentDataSource;
    }

    public void setData(Map<String,String> vBatchNamesToStdCnt){
        mDataset.clear();
        mBatch2StdCnt.clear();
        mDataset.addAll(vBatchNamesToStdCnt.keySet());
        mBatch2StdCnt.putAll(vBatchNamesToStdCnt);
    }

    public void setData(ArrayList<String> vBatchNames){
        mDataset.clear();
        mDataset.addAll(vBatchNames);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((SimpleViewHolder) holder).bindData(mDataset.get(position),position);
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.list_item;
    }

    public boolean isSelected(String batchName) {
        return mSelectedBatches.contains(batchName);
    }

    public void selectItem( String vBatchId) {
        mSelectedBatches.add(vBatchId);
    }
    public void deSelectItem( String vStudentId) {
        mSelectedBatches.remove(vStudentId);
        if (mSelectedBatches.size() == 0) {
            resetAllSelections();
        }
    }

    public void resetAllSelections() {

        getmSelectedBatches().clear();
        notifyDataSetChanged();
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        private TextView batchTextView;
        private TextView mPendingStu;


        public SimpleViewHolder(final View itemView) {
            super(itemView);
            batchTextView = itemView.findViewById(R.id.name);

            mPendingStu = itemView.findViewById(R.id.pendingStu);


        }

        public void bindData(final String vValue,int position) {

            batchTextView.setText(vValue);

            if(Integer.valueOf(mBatch2StdCnt.get(vValue))<  maxStudents ){
                mPendingStu.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.signal_image_green,0);
                mPendingStu.setCompoundDrawablePadding(15);
            }else{
                mPendingStu.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.signal_image_red,0);
                mPendingStu.setCompoundDrawablePadding(15);
            }


            if(mCurrentDataSource.equals(DBConstants.SOURCE_SERVER)){
                batchTextView.setBackgroundColor(Color.TRANSPARENT);
                mPendingStu.setText(String.valueOf(maxStudents-Integer.valueOf(mBatch2StdCnt.get(vValue))));
                mPendingStu.setBackgroundColor(Color.TRANSPARENT);
                mPendingStu.setTextColor(Color.BLACK);
            }


            if (isSelected(vValue)) {
                this.itemView.setBackgroundColor(Color.parseColor("#ADD8E6"));
            } else {
                this.itemView.setBackgroundColor(Color.TRANSPARENT);
            }


            this.itemView.setOnClickListener(v -> {
                if(!FirestoreData.isPermittedToViewBatch( batchTextView.getText().toString())) return;

                resetAllSelections();
                Intent myIntent = new Intent(v.getContext(), StudentsListActivity.class);
                myIntent.putExtra(AppConstants.INTENT_KEY.BATCH, batchTextView.getText().toString());
                mContext.startActivity(myIntent);
                batchListActivity.overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left);
            });
            mPendingStu.setOnClickListener(v -> {
                if(!FirestoreData.isPermittedToViewBatch( batchTextView.getText().toString())) return;

                if(!mViewModel.isAdminUser()){
                    Intent myIntent = new Intent(v.getContext(), StudentsListActivity.class);
                    myIntent.putExtra(AppConstants.INTENT_KEY.BATCH, batchTextView.getText().toString());
                    mContext.startActivity(myIntent);
                    batchListActivity.overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left);
                }else{

                    if (!isSelected(vValue)) {
                        //select item
                        selectItem(vValue);
                        this.itemView.setBackgroundColor(Color.parseColor("#ADD8E6"));
                    } else {
                        deSelectItem(vValue);
                        this.itemView.setBackgroundColor(Color.TRANSPARENT);
                    }
                }

            });
            this.itemView.setOnLongClickListener(v -> {
                if(!mViewModel.isAdminUser()){

                    Toast.makeText(v.getContext(), "Permission not available", Toast.LENGTH_SHORT).show();
                } else{
                    Intent tMaintIntent = new Intent(v.getContext(), BatchMaintenanceActivity.class);
                    tMaintIntent.putExtra(AppConstants.INTENT_KEY.BATCH, batchTextView.getText().toString());
                    v.getContext().startActivity(tMaintIntent);
                    batchListActivity.overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left);

                }
                return true;
            });

        }
    }
}
