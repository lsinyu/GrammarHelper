package com.example.grammarhelper.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.grammarhelper.R;
import com.example.grammarhelper.adapter.BadgeAdapter;
import com.example.grammarhelper.database.DatabaseHelper;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private LineChart scoreLineChart, testLineChart;
    private PieChart errorPieChart;
    private TextView topMistakesList, streakText, userName, userStatus;
    private ImageView profileImage;
    private RecyclerView badgesRecyclerView;
    private ErrorLogDAO errorLogDAO;
    private SessionDAO sessionDAO;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        errorLogDAO = new ErrorLogDAO(this);
        sessionDAO = new SessionDAO(this);
        dbHelper = new DatabaseHelper(this);
        errorLogDAO.open();
        sessionDAO.open();

        initViews();
        setupUserProfile();
        setupStreak();
        setupLineChart();
        setupTestChart();
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
        testLineChart = findViewById(R.id.testLineChart);
        errorPieChart = findViewById(R.id.errorPieChart);
        topMistakesList = findViewById(R.id.topMistakesList);
        badgesRecyclerView = findViewById(R.id.badgesRecyclerView);
        streakText = findViewById(R.id.streakText);
        userName = findViewById(R.id.userName);
        userStatus = findViewById(R.id.userStatus);
        profileImage = findViewById(R.id.profileImage);

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

    private void setupUserProfile() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            userName.setText(account.getDisplayName());
            userStatus.setText("Linked to Google Account");
            if (account.getPhotoUrl() != null) {
                Glide.with(this)
                     .load(account.getPhotoUrl())
                     .placeholder(R.drawable.circle_background)
                     .circleCrop()
                     .into(profileImage);
            }
        } else {
            userName.setText("Guest User");
            userStatus.setText("Anonymous Mode");
            profileImage.setImageResource(android.R.drawable.ic_menu_gallery);
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
        
        for (int i = 0; i < sessions.size(); i++) {
            com.example.grammarhelper.model.Session s = sessions.get(sessions.size() - 1 - i);
            entries.add(new Entry(i + 1, s.grammarScore));
        }

        if (entries.isEmpty()) {
            entries.add(new Entry(1, 100)); 
        }

        LineDataSet dataSet = new LineDataSet(entries, "Grammar Score Over Time");
        styleLineDataSet(dataSet, "#2563EB");

        LineData lineData = new LineData(dataSet);
        scoreLineChart.setData(lineData);
        scoreLineChart.getDescription().setEnabled(false);
        scoreLineChart.getAxisRight().setEnabled(false);
        scoreLineChart.animateX(1000);
        scoreLineChart.invalidate();
    }

    private void setupTestChart() {
        if (testLineChart == null) return;

        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_TEST_RESULTS, 
                new String[]{DatabaseHelper.COL_TEST_SCORE}, 
                null, null, null, null, DatabaseHelper.COL_TEST_TIMESTAMP + " ASC");

        int index = 1;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int score = cursor.getInt(0);
                entries.add(new Entry(index++, score));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (entries.isEmpty()) {
            testLineChart.setNoDataText("No test data available. Take a test to see your progress!");
            testLineChart.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Evaluation Scores");
        styleLineDataSet(dataSet, "#DC2626"); 

        LineData lineData = new LineData(dataSet);
        testLineChart.setData(lineData);
        testLineChart.getDescription().setEnabled(false);
        testLineChart.getAxisRight().setEnabled(false);
        testLineChart.getAxisLeft().setAxisMaximum(10f);
        testLineChart.getAxisLeft().setAxisMinimum(0f);
        testLineChart.animateX(1000);
        testLineChart.invalidate();
    }

    private void styleLineDataSet(LineDataSet dataSet, String colorHex) {
        int color = Color.parseColor(colorHex);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(30);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
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
             entries.add(new PieEntry(45, "Correctness"));
             entries.add(new PieEntry(30, "Clarity"));
             entries.add(new PieEntry(15, "Tone"));
             entries.add(new PieEntry(10, "Engagement"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Error Patterns");
        int[] colors = {
            Color.parseColor("#DC2626"), 
            Color.parseColor("#2563EB"), 
            Color.parseColor("#D97706"), 
            Color.parseColor("#16A34A")
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
            totalFixes >= 1,
            streakDays >= 7,
            totalFixes >= 100,
            sessionCount >= 20,
            sessionCount >= 10,
            totalFixes >= 50
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
