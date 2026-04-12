package com.example.grammarhelper.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grammarhelper.R;
import com.example.grammarhelper.adapter.ErrorCardAdapter;
import com.example.grammarhelper.model.GrammarError;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class ErrorReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ErrorCardAdapter adapter;
    private TabLayout tabs;
    private List<GrammarError> allErrors = new ArrayList<>();
    
    // Shared memory for large lists between activities
    public static List<GrammarError> pendingErrors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_review);

        // Load errors from shared memory
        if (pendingErrors != null) {
            allErrors = new ArrayList<>(pendingErrors);
        } else {
            allErrors = new ArrayList<>();
        }

        initViews();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.errorReviewToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.errorRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ErrorCardAdapter(this, allErrors);
        recyclerView.setAdapter(adapter);

        tabs = findViewById(R.id.errorTabs);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterErrors(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        ExtendedFloatingActionButton btnFixAll = findViewById(R.id.btnFixAll);
        btnFixAll.setOnClickListener(v -> {
            allErrors.clear();
            adapter.notifyDataSetChanged();
            getSupportActionBar().setTitle("Error Review (0 Issues)");
        });

        getSupportActionBar().setTitle("Error Review (" + allErrors.size() + " Issues)");
    }

    private void filterErrors(String category) {
        if ("All".equals(category)) {
            adapter.updateErrors(allErrors);
        } else {
            List<GrammarError> filtered = new ArrayList<>();
            for (GrammarError error : allErrors) {
                if (error.errorType.equalsIgnoreCase(category)) {
                    filtered.add(error);
                }
            }
            adapter.updateErrors(filtered);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
