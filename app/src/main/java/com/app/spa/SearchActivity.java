package com.app.spa;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.spa.adapter.SearchActivityAdapter;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SearchActivityAdapter mAdapter;
    private ApplicationViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewModel = ViewModelProviders.of(this).get(ApplicationViewModel.class);
        RecyclerView recyclerView = findViewById(R.id.search_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new SearchActivityAdapter(mViewModel.getStudentNamesToStudents(),this);
        recyclerView.setAdapter(mAdapter);
        Intent tIntent = getIntent();
        handleIntent(tIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(this);
        search.setQueryHint(getResources().getString(R.string.search_hint));

        search.setIconifiedByDefault(false);
        return true;

    }
    @Override
    public boolean onSupportNavigateUp() {
        //overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

        finish();
        return true;

    }
    @Override
    protected void onNewIntent(Intent vIntent) {
        super.onNewIntent(vIntent);

        handleIntent(vIntent);
    }

    private void handleIntent(Intent vIntent) {

        if (Intent.ACTION_SEARCH.equals(vIntent.getAction())) {
            String query = vIntent.getStringExtra(SearchManager.QUERY);

            doSearch(query);
        }
    }


    private void doSearch(String vQuery){
        mAdapter.filter(vQuery);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        mAdapter.filter(newText);
        return true;
    }
}


