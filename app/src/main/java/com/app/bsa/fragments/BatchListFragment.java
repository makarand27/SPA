package com.app.bsa.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.bsa.R;
import com.app.bsa.adapter.BatchListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BatchListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BatchListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BatchListAdapter adapter;
    private RecyclerView recyclerView;
    RecyclerView.ItemDecoration itemDecoration;

    public BatchListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BatchListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BatchListFragment newInstance(String param1, String param2) {
        BatchListFragment fragment = new BatchListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }



    // Method to set the adapter
    public void setAdapter(BatchListAdapter adapter,RecyclerView.ItemDecoration itemDecoration) {
        this.adapter = adapter;
        this.itemDecoration = itemDecoration;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         List<String> dataList;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_batch_list, container, false);
        recyclerView = view.findViewById(R.id.rcyr_batch_list);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(itemDecoration);
        dataList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            dataList.add("Item " + (i + 1));
        }
        recyclerView.setAdapter(adapter);
        return view;
    }
}