package com.example.grammarhelper.ui;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import com.example.grammarhelper.MainActivity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import com.example.grammarhelper.R;
import com.example.grammarhelper.database.ChatHistoryDAO;
import com.example.grammarhelper.database.ErrorLogDAO;
import com.example.grammarhelper.util.NotificationHelper;
import com.example.grammarhelper.util.PDFReportGenerator;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SettingsActivity extends AppCompatActivity {

    private static final String APP_PREFS = "GrammarHelperPrefs";
    private static final String SETTINGS_PREFS = "grammar_helper_prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_THEME = "theme_pos";

    private ChatHistoryDAO chatDb;
    private ErrorLogDAO errorDb;
    private SharedPreferences prefs;
    private GoogleSignInClient mGoogleSignInClient;

    private Spinner spinnerTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme BEFORE super.onCreate and setContentView
        prefs = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        applyTheme(prefs.getInt(KEY_THEME, 0));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        chatDb = new ChatHistoryDAO(this);
        errorDb = new ErrorLogDAO(this);
        chatDb.open();
        errorDb.open();

        // Configure Google Sign-In for logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setupToolbar();
        initViews();
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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Settings");
            }
        }
    }

    private void initViews() {
        // --- Writing Preferences ---
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        Spinner spinnerDefaultMode = findViewById(R.id.spinnerDefaultMode);

        if (spinnerLanguage != null) {
            ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"American English", "British English", "Australian English"});
            spinnerLanguage.setAdapter(langAdapter);
            int langPos = prefs.getInt("language_pos", 0);
            spinnerLanguage.setSelection(langPos);
        }

        if (spinnerDefaultMode != null) {
            ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"Academic", "Professional", "Casual"});
            spinnerDefaultMode.setAdapter(modeAdapter);
            int modePos = prefs.getInt("mode_pos", 1);
            spinnerDefaultMode.setSelection(modePos);
        }

        // --- AI Preferences ---
        Spinner spinnerLevel = findViewById(R.id.spinnerLevel);
        if (spinnerLevel != null) {
            ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"Beginner", "Intermediate", "Expert"});
            spinnerLevel.setAdapter(levelAdapter);
            int levelPos = prefs.getInt("level_pos", 1);
            spinnerLevel.setSelection(levelPos);
        }

        SwitchMaterial switchDailyTip = findViewById(R.id.switchDailyTip);
        if (switchDailyTip != null) {
            switchDailyTip.setChecked(prefs.getBoolean("daily_tip", true));
            switchDailyTip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("daily_tip", isChecked).apply();
                if (isChecked) {
                    NotificationHelper.scheduleDailyTip(this, 8, 0);
                    Toast.makeText(this, "Daily grammar tips enabled", Toast.LENGTH_SHORT).show();
                } else {
                    NotificationHelper.cancelDailyTip(this);
                    Toast.makeText(this, "Daily grammar tips disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- Theme (Improved) ---
        spinnerTheme = findViewById(R.id.spinnerTheme);
        if (spinnerTheme != null) {
            ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"🌓 System Default", "☀️ Light Mode", "🌙 Dark Mode"});
            spinnerTheme.setAdapter(themeAdapter);
            int themePos = prefs.getInt(KEY_THEME, 0);
            spinnerTheme.setSelection(themePos);

            spinnerTheme.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                    if (position != prefs.getInt(KEY_THEME, 0)) {
                        // Save the new theme preference
                        prefs.edit().putInt(KEY_THEME, position).apply();

                        // Apply the theme
                        applyTheme(position);

                        // Show restart dialog
                        showThemeRestartDialog();
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }

        // --- Data & Privacy ---
        Button btnClearChat = findViewById(R.id.btnClearHistory);
        if (btnClearChat != null) {
            btnClearChat.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Clear Chat History")
                        .setMessage("This will delete all chat messages. This action cannot be undone.")
                        .setPositiveButton("Clear", (dialog, which) -> {
                            chatDb.clearHistory();
                            Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        Button btnClearErrors = findViewById(R.id.btnClearErrors);
        if (btnClearErrors != null) {
            btnClearErrors.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Clear Error Log")
                        .setMessage("This will delete all error history and reset your dashboard. This action cannot be undone.")
                        .setPositiveButton("Clear", (dialog, which) -> {
                            errorDb.clearErrorLog();
                            Toast.makeText(this, "Error log cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }



        // --- Log Out ---
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }
    }

    private void showThemeRestartDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Theme Changed")
                .setMessage("Theme will change after restarting the app. Restart now?")
                .setPositiveButton("Restart Now", (dialog, which) -> {
                    recreateActivityStack();
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private void recreateActivityStack() {
        // Restart the app by launching main activity and clearing stack
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ==================== PDF EXPORT METHODS ====================

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
                    progressDialog.dismiss();

                    if (pdfFile != null && pdfFile.exists()) {
                        showExportOptions(pdfFile);
                    } else {
                        Toast.makeText(this, "Failed to generate report", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
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

    // ==================== END OF PDF EXPORT METHODS ====================

    private void performLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out and exit?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                        SharedPreferences appPrefs = getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
                        appPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();

                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(homeIntent);

                        finishAffinity();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSpinnerPrefs();
    }

    private void saveSpinnerPrefs() {
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        Spinner spinnerDefaultMode = findViewById(R.id.spinnerDefaultMode);
        Spinner spinnerLevel = findViewById(R.id.spinnerLevel);
        // Theme is now saved separately via onItemSelectedListener

        SharedPreferences.Editor editor = prefs.edit();
        if (spinnerLanguage != null) editor.putInt("language_pos", spinnerLanguage.getSelectedItemPosition());
        if (spinnerDefaultMode != null) editor.putInt("mode_pos", spinnerDefaultMode.getSelectedItemPosition());
        if (spinnerLevel != null) editor.putInt("level_pos", spinnerLevel.getSelectedItemPosition());
        editor.apply();
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
        chatDb.close();
        errorDb.close();
    }
}