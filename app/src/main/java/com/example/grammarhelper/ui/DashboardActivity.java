package com.example.grammarhelper.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grammarhelper.R;
import com.example.grammarhelper.adapter.BadgeAdapter;
import com.example.grammarhelper.database.ErrorLogDAO;
import com.example.grammarhelper.database.SessionDAO;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private LineChart scoreLineChart;
    private PieChart errorPieChart;
    private TextView topMistakesList, streakText;
    private RecyclerView badgesRecyclerView;
    private ErrorLogDAO errorLogDAO;
    private SessionDAO sessionDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        errorLogDAO = new ErrorLogDAO(this);
        sessionDAO = new SessionDAO(this);
        errorLogDAO.open();
        sessionDAO.open();

        initViews();
        setupStreak();
        setupLineChart();
        setupPieChart();
        loadTopMistakes();
        setupBadges();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.dashboardToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("My Progress");
            }
        }

        scoreLineChart = findViewById(R.id.scoreLineChart);
        errorPieChart = findViewById(R.id.errorPieChart);
        topMistakesList = findViewById(R.id.topMistakesList);
        badgesRecyclerView = findViewById(R.id.badgesRecyclerView);
        streakText = findViewById(R.id.streakText);

        android.widget.ImageButton btnShareProgress = findViewById(R.id.btnShareProgress);
        if (btnShareProgress != null) {
            btnShareProgress.setOnClickListener(v -> {
                int streak = sessionDAO.getStreakDays();
                int fixes = errorLogDAO.getTotalFixedCount();
                String text = "I'm on a " + streak + "-day learning streak on GrammarHelper! I've fixed " + fixes + " grammar mistakes. Join me!";
                android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                startActivity(android.content.Intent.createChooser(shareIntent, "Share your progress via"));
            });
        }
    }

    private void setupStreak() {
        if (streakText == null) return;

        int streak = sessionDAO.getStreakDays();
        if (streak > 0) {
            streakText.setText("🔥 " + streak + "-Day Streak! Keep it up!");
        } else {
            streakText.setText("🔥 Start writing to begin your streak!");
        }
    }

    private void setupLineChart() {
        List<Entry> entries = new ArrayList<>();
        List<com.example.grammarhelper.model.Session> sessions = sessionDAO.getAllSessions();
        
        // Reverse to show oldest to newest (since DAO returns DESC)
        for (int i = 0; i < sessions.size(); i++) {
            com.example.grammarhelper.model.Session s = sessions.get(sessions.size() - 1 - i);
            entries.add(new Entry(i + 1, s.grammarScore));
        }

        if (entries.isEmpty()) {
            entries.add(new Entry(1, 100)); // Default placeholder if no data
        }

        LineDataSet dataSet = new LineDataSet(entries, "Grammar Score Over Time");
        dataSet.setColor(Color.parseColor("#2563EB"));
        dataSet.setCircleColor(Color.parseColor("#2563EB"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#2563EB"));
        dataSet.setFillAlpha(30);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        scoreLineChart.setData(lineData);
        scoreLineChart.getDescription().setEnabled(false);
        scoreLineChart.getAxisRight().setEnabled(false);
        scoreLineChart.animateX(1000);
        scoreLineChart.invalidate();
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        Cursor cursor = errorLogDAO.getErrorDistribution();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String type = cursor.getString(0);
                int count = cursor.getInt(1);
                entries.add(new PieEntry(count, type));
            } while (cursor.moveToNext());
            cursor.close();
        } else {
             // Placeholder when no data
             entries.add(new PieEntry(45, "Correctness"));
             entries.add(new PieEntry(30, "Clarity"));
             entries.add(new PieEntry(15, "Tone"));
             entries.add(new PieEntry(10, "Engagement"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Error Patterns");
        int[] colors = {
            Color.parseColor("#DC2626"), // Red - Correctness
            Color.parseColor("#2563EB"), // Blue - Clarity
            Color.parseColor("#D97706"), // Yellow - Tone
            Color.parseColor("#16A34A")  // Green - Engagement
        };
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        errorPieChart.setData(pieData);
        errorPieChart.setCenterText("Errors");
        errorPieChart.setCenterTextSize(14f);
        errorPieChart.getDescription().setEnabled(false);
        errorPieChart.setHoleRadius(40f);
        errorPieChart.setTransparentCircleRadius(45f);
        errorPieChart.animateY(1000);
        errorPieChart.invalidate();
    }

    private void loadTopMistakes() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = errorLogDAO.getTopMistakes();
        int rank = 1;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String subtype = cursor.getString(0);
                int count = cursor.getInt(1);
                sb.append(rank).append(". ").append(subtype).append("   ").append(count).append("×\n");
                rank++;
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            sb.append("No errors recorded yet. Start writing!");
        }
        topMistakesList.setText(sb.toString());
    }

    private void setupBadges() {
        int sessionCount = sessionDAO.getSessionCount();
        int streakDays = sessionDAO.getStreakDays();
        int totalFixes = errorLogDAO.getTotalFixedCount();

        List<String> badges = Arrays.asList(
            "🥇 First Fix", 
            "📅 7-Day Streak", 
            "💯 100 Fixes", 
            "📝 Grammar Master", 
            "🎯 Tone Pro", 
            "👑 Clarity King"
        );
        List<Boolean> unlocked = Arrays.asList(
            totalFixes >= 1,       // First Fix
            streakDays >= 7,       // 7-Day Streak
            totalFixes >= 100,     // 100 Fixes
            sessionCount >= 20,    // Grammar Master
            sessionCount >= 10,    // Tone Pro
            totalFixes >= 50       // Clarity King
        );

        BadgeAdapter adapter = new BadgeAdapter(badges, unlocked);
        badgesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        badgesRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        errorLogDAO.close();
        sessionDAO.close();
    }
}
