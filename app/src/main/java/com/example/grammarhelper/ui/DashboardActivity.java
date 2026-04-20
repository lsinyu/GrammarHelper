package com.example.grammarhelper.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.grammarhelper.R;
import com.example.grammarhelper.adapter.BadgeAdapter;
import com.example.grammarhelper.database.DatabaseHelper;
import com.example.grammarhelper.database.ErrorLogDAO;
import com.example.grammarhelper.database.SessionDAO;
import com.example.grammarhelper.model.Session;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private static final String SETTINGS_PREFS = "grammar_helper_prefs";
    private static final String KEY_THEME = "theme_pos";

    private LineChart scoreLineChart;
    private LineChart testLineChart;
    private PieChart errorPieChart;
    private TextView topMistakesList;
    private TextView streakText;
    private TextView userName;
    private TextView userStatus;
    private ImageView profileImage;
    private RecyclerView badgesRecyclerView;

    private ErrorLogDAO errorLogDAO;
    private SessionDAO sessionDAO;
    private DatabaseHelper dbHelper;

    private boolean isDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme BEFORE super.onCreate
        android.content.SharedPreferences prefs = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        applyTheme(prefs.getInt(KEY_THEME, 0));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Check current theme
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        isDarkMode = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        // Initialize DAOs
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

    private void applyTheme(int themePos) {
        switch (themePos) {
            case 0: // System Default
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1: // Light Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2: // Dark Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
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
        List<Session> sessions = sessionDAO.getAllSessions();

        int maxSessions = Math.min(20, sessions.size());
        for (int i = 0; i < maxSessions; i++) {
            Session s = sessions.get(i);
            entries.add(new Entry(i + 1, s.grammarScore));
        }

        if (entries.isEmpty()) {
            scoreLineChart.setNoDataText("No data yet. Start writing!");
            if (isDarkMode) {
                scoreLineChart.setNoDataTextColor(Color.parseColor("#EAEAEA"));
            } else {
                scoreLineChart.setNoDataTextColor(Color.parseColor("#6C757D"));
            }
            scoreLineChart.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Grammar Score");

        if (isDarkMode) {
            dataSet.setColor(Color.parseColor("#60A5FA"));
            dataSet.setCircleColor(Color.parseColor("#60A5FA"));
            dataSet.setValueTextColor(Color.parseColor("#EAEAEA"));
            dataSet.setFillColor(Color.parseColor("#3B82F6"));
        } else {
            dataSet.setColor(Color.parseColor("#2563EB"));
            dataSet.setCircleColor(Color.parseColor("#2563EB"));
            dataSet.setValueTextColor(Color.parseColor("#1A1A2E"));
            dataSet.setFillColor(Color.parseColor("#2563EB"));
        }

        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        scoreLineChart.setData(lineData);
        scoreLineChart.getDescription().setEnabled(false);
        scoreLineChart.getAxisRight().setEnabled(false);

        if (isDarkMode) {
            scoreLineChart.getXAxis().setTextColor(Color.parseColor("#B0B0B8"));
            scoreLineChart.getAxisLeft().setTextColor(Color.parseColor("#B0B0B8"));
        } else {
            scoreLineChart.getXAxis().setTextColor(Color.parseColor("#6C757D"));
            scoreLineChart.getAxisLeft().setTextColor(Color.parseColor("#6C757D"));
        }

        scoreLineChart.getXAxis().setGranularity(1f);
        scoreLineChart.getXAxis().setLabelCount(Math.min(6, maxSessions), true);
        scoreLineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        scoreLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
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
            testLineChart.setNoDataText("Take a test to see your score!");
            if (isDarkMode) {
                testLineChart.setNoDataTextColor(Color.parseColor("#EAEAEA"));
            } else {
                testLineChart.setNoDataTextColor(Color.parseColor("#6C757D"));
            }
            testLineChart.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Test Scores");

        if (isDarkMode) {
            dataSet.setColor(Color.parseColor("#F87171"));
            dataSet.setCircleColor(Color.parseColor("#F87171"));
            dataSet.setValueTextColor(Color.parseColor("#EAEAEA"));
            dataSet.setFillColor(Color.parseColor("#EF4444"));
        } else {
            dataSet.setColor(Color.parseColor("#DC2626"));
            dataSet.setCircleColor(Color.parseColor("#DC2626"));
            dataSet.setValueTextColor(Color.parseColor("#1A1A2E"));
            dataSet.setFillColor(Color.parseColor("#DC2626"));
        }

        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        testLineChart.setData(lineData);
        testLineChart.getDescription().setEnabled(false);
        testLineChart.getAxisRight().setEnabled(false);

        if (isDarkMode) {
            testLineChart.getXAxis().setTextColor(Color.parseColor("#B0B0B8"));
            testLineChart.getAxisLeft().setTextColor(Color.parseColor("#B0B0B8"));
        } else {
            testLineChart.getXAxis().setTextColor(Color.parseColor("#6C757D"));
            testLineChart.getAxisLeft().setTextColor(Color.parseColor("#6C757D"));
        }

        testLineChart.getXAxis().setGranularity(1f);
        testLineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        testLineChart.animateX(1000);
        testLineChart.invalidate();
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
                Color.parseColor("#EF4444"),
                Color.parseColor("#3B82F6"),
                Color.parseColor("#F97316"),
                Color.parseColor("#8B5CF6")
        };
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);

        // Set percentage text color
        if (isDarkMode) {
            dataSet.setValueTextColor(Color.parseColor("#EAEAEA"));
        } else {
            dataSet.setValueTextColor(Color.parseColor("#1A1A2E"));
        }

        PieData pieData = new PieData(dataSet);
        errorPieChart.setData(pieData);

        // Set colors based on theme
        if (isDarkMode) {
            errorPieChart.setCenterTextColor(Color.parseColor("#EAEAEA"));
            errorPieChart.setHoleColor(Color.parseColor("#121212"));
            // MAKE LEGEND LABELS WHITE
            errorPieChart.setEntryLabelColor(Color.WHITE);
            errorPieChart.getLegend().setTextColor(Color.WHITE);
        } else {
            errorPieChart.setCenterTextColor(Color.parseColor("#1A1A2E"));
            errorPieChart.setHoleColor(Color.parseColor("#FFFFFF"));
            errorPieChart.setEntryLabelColor(Color.parseColor("#1A1A2E"));
            errorPieChart.getLegend().setTextColor(Color.parseColor("#1A1A2E"));
        }

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
                sb.append(rank).append(". ").append(subtype).append("   ").append(count).append("×\n\n");
                rank++;
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            sb.append("No errors recorded yet.\n\nStart writing to see your progress!");
        }
        topMistakesList.setText(sb.toString());
    }

    private void setupBadges() {
        int sessionCount = sessionDAO.getSessionCount();
        int streakDays = sessionDAO.getStreakDays();
        int totalFixes = errorLogDAO.getTotalFixedCount();

        List<String> badges = Arrays.asList(
                "🥇 First Fix", "📅 7-Day Streak", "💯 100 Fixes",
                "📝 Grammar Master", "🎯 Tone Pro", "👑 Clarity King"
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