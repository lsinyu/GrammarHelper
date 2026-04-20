package com.example.grammarhelper.ui;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.grammarhelper.R;
import com.example.grammarhelper.adapter.BadgeAdapter;
import com.example.grammarhelper.database.DatabaseHelper;
import com.example.grammarhelper.database.ErrorLogDAO;
import com.example.grammarhelper.database.SessionDAO;
import com.example.grammarhelper.model.Session;
import com.example.grammarhelper.util.PDFReportGenerator;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        setupPDFExport();  // PDF button setup
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
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
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

    private void setupPDFExport() {
        ImageButton btnExportPDF = findViewById(R.id.btnExportPDF);
        if (btnExportPDF != null) {
            btnExportPDF.setOnClickListener(v -> exportPDFReport());
        }
    }

    private void exportPDFReport() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating PDF report...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                PDFReportGenerator generator = new PDFReportGenerator(this);
                File pdfFile = generator.generateReport();

                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    if (pdfFile != null && pdfFile.exists()) {
                        showExportOptions(pdfFile);
                    } else {
                        Toast.makeText(this, "Failed to generate report", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showExportOptions(File pdfFile) {
        String[] options = {"Share PDF", "Save to Device", "Open PDF"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Export Report")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sharePDF(pdfFile);
                            break;
                        case 1:
                            savePDFToDownloads(pdfFile);
                            break;
                        case 2:
                            openPDF(pdfFile);
                            break;
                    }
                })
                .show();
    }

    private void sharePDF(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", pdfFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share Report via"));
    }

    private void savePDFToDownloads(File pdfFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFile.getName());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            try (ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(uri, "w");
                 FileOutputStream out = new FileOutputStream(fd.getFileDescriptor());
                 FileInputStream in = new FileInputStream(pdfFile)) {

                byte[] buffer = new byte[8192];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                Toast.makeText(this, "PDF saved to Downloads folder", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File destFile = new File(downloadsDir, pdfFile.getName());

            try (FileInputStream in = new FileInputStream(pdfFile);
                 FileOutputStream out = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[8192];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                Toast.makeText(this, "PDF saved to: " + destFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPDF(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show();
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
        // Reverse order - newest on right
        for (int i = maxSessions - 1; i >= 0; i--) {
            Session s = sessions.get(i);
            entries.add(new Entry(maxSessions - i, s.grammarScore));
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

        List<Integer> scores = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                scores.add(cursor.getInt(0));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (scores.isEmpty()) {
            testLineChart.setNoDataText("Take a test to see your score!");
            if (isDarkMode) {
                testLineChart.setNoDataTextColor(Color.parseColor("#EAEAEA"));
            } else {
                testLineChart.setNoDataTextColor(Color.parseColor("#6C757D"));
            }
            testLineChart.invalidate();
            return;
        }

        // Reverse order - newest on right
        for (int i = scores.size() - 1; i >= 0; i--) {
            entries.add(new Entry(scores.size() - i, scores.get(i)));
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

        if (isDarkMode) {
            dataSet.setValueTextColor(Color.parseColor("#EAEAEA"));
        } else {
            dataSet.setValueTextColor(Color.parseColor("#1A1A2E"));
        }

        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        errorPieChart.setData(pieData);

        if (isDarkMode) {
            errorPieChart.setCenterTextColor(Color.parseColor("#EAEAEA"));
            errorPieChart.setHoleColor(Color.parseColor("#121212"));
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

        // Set text color based on theme
        if (isDarkMode) {
            topMistakesList.setTextColor(Color.parseColor("#EAEAEA"));
        } else {
            topMistakesList.setTextColor(Color.parseColor("#1A1A2E"));
        }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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