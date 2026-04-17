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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_review);

        // Safely extract the errors from the Intent
        if (getIntent().hasExtra("errors_list")) {
            allErrors = (ArrayList<GrammarError>) getIntent().getSerializableExtra("errors_list");
        }

        if (allErrors == null) {
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
        adapter = new ErrorCardAdapter(this, allErrors, new ErrorCardAdapter.OnErrorActionListener() {
            @Override
            public void onFixClicked(GrammarError errorToFix) {
                // 1. Mark this specific error as fixed
                errorToFix.wasAccepted = 1;

                // 2. Package it into a list (so it matches the Batch Fix format our editor expects)
                java.util.ArrayList<GrammarError> fixedList = new java.util.ArrayList<>();
                fixedList.add(errorToFix);

                // 3. Send it back to the editor!
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updated_errors", fixedList);
                setResult(RESULT_OK, resultIntent);
                finish(); // Closes the review screen and applies the fix in the editor
            }
        });
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
            // 1. Mark every error in the list as accepted/fixed
            for (GrammarError error : allErrors) {
                error.wasAccepted = 1;
            }

            // 2. Package the updated list into an Intent
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_errors", new ArrayList<>(allErrors));

            // 3. Set the result to OK and send the Intent back
            setResult(RESULT_OK, resultIntent);

            // 4. Close the review screen and return to the editor
            finish();
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
