package com.app.bsa.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.app.bsa.R;
import com.app.bsa.StudentMaintActivity;
import com.app.bsa.StudentsListActivity;
import com.app.bsa.service.repository.Student;
import com.app.bsa.utility.AppConstants;

public class SearchActivityAdapter extends RecyclerView.Adapter {

    private ArrayList mDataset = new ArrayList();
    private HashMap<String, Student> m_StudentDataset;

    Activity searchActivity;

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView batchTextView;
        private TextView bankrefTextView;

        public SimpleViewHolder(final View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.student_name);
            batchTextView = itemView.findViewById(R.id.student_batch);
            bankrefTextView = itemView.findViewById(R.id.student_bankref);
        }

        public void bindData(final Student vStudent) {
            nameTextView.setText(vStudent.getFullName());
            batchTextView.setText(vStudent.Batch_name);
            bankrefTextView.setText(vStudent.Bank_ref);

            nameTextView.setOnClickListener(v -> {
                Intent tMaintIntent = new Intent(v.getContext(), StudentMaintActivity.class);
                tMaintIntent.putExtra("ACTION", "MAINT_ACTION_VIEW");
                tMaintIntent.putExtra("STUDENT_ID", vStudent.Id);
                (v.getContext()).startActivity(tMaintIntent);
                //searchActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            });

            batchTextView.setOnClickListener(v -> {
                Intent tMaintIntent = new Intent(v.getContext(), StudentsListActivity.class);
                tMaintIntent.putExtra(AppConstants.INTENT_KEY.BATCH, batchTextView.getText().toString());
                tMaintIntent.putExtra(AppConstants.INTENT_KEY.STUDENT_NAME, vStudent.getFullName());
                (v.getContext()).startActivity(tMaintIntent);
               // searchActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            });

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ((SimpleViewHolder) holder).bindData((Student)mDataset.get(position));

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    @Override
    public int getItemViewType(final int position) {
        return R.layout.search_name_list_item;
    }

    public SearchActivityAdapter(HashMap<String, Student> vDataset, Activity searchActivity) {
        m_StudentDataset = vDataset;
        this.searchActivity = searchActivity;
    }

    @Override
    public SearchActivityAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new SimpleViewHolder(view);
    }

    public void filter(String query){

        query = query.toLowerCase();

        mDataset.clear();
        Set keySet = m_StudentDataset.keySet();
        Iterator keyIterator = keySet.iterator();
        while (keyIterator.hasNext()) {

            Student tStudent = m_StudentDataset.get((String)keyIterator.next());

            if (tStudent.getFullName().toLowerCase().contains(query)
                    || tStudent.Bank_ref.toLowerCase().contains(query) || tStudent.Custom_fee.contains(query)) {

                mDataset.add(tStudent);
            }
        }
        notifyDataSetChanged();
    }



}
