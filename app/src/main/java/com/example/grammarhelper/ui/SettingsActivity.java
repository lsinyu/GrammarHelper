package com.example.grammarhelper.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.example.grammarhelper.R;
import com.example.grammarhelper.database.ChatHistoryDAO;
import com.example.grammarhelper.database.ErrorLogDAO;
import com.example.grammarhelper.util.NotificationHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final String APP_PREFS = "GrammarHelperPrefs";
    private static final String SETTINGS_PREFS = "grammar_helper_prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private ChatHistoryDAO chatDb;
    private ErrorLogDAO errorDb;
    private SharedPreferences prefs;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        chatDb = new ChatHistoryDAO(this);
        errorDb = new ErrorLogDAO(this);
        chatDb.open();
        errorDb.open();
        prefs = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);

        // Configure Google Sign-In for logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setupToolbar();
        initViews();
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
            int modePos = prefs.getInt("mode_pos", 1); // Default: Professional
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

        // --- Floating Bubble ---
        SwitchMaterial switchBubble = findViewById(R.id.switchBubble);
        if (switchBubble != null) {
            switchBubble.setChecked(prefs.getBoolean("bubble_enabled", true));
            switchBubble.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("bubble_enabled", isChecked).apply();
                if (isChecked) {
                    Toast.makeText(this, "Floating bubble enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Floating bubble disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- Theme ---
        Spinner spinnerTheme = findViewById(R.id.spinnerTheme);
        if (spinnerTheme != null) {
            ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"System Default", "Light", "Dark"});
            spinnerTheme.setAdapter(themeAdapter);
            int themePos = prefs.getInt("theme_pos", 0);
            spinnerTheme.setSelection(themePos);
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

        Button btnExport = findViewById(R.id.btnExportPdf);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> {
                Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // --- Log Out ---
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }
    }

    private void performLogout() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out and exit?")
            .setPositiveButton("Log Out", (dialog, which) -> {
                // 1. Sign out from Google
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    // 2. Clear login preference
                    SharedPreferences appPrefs = getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
                    appPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();

                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    // 3. Bring user to the device main page (Home screen)
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                    
                    // 4. Close the app
                    finishAffinity();
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save spinner selections
        saveSpinnerPrefs();
    }

    private void saveSpinnerPrefs() {
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        Spinner spinnerDefaultMode = findViewById(R.id.spinnerDefaultMode);
        Spinner spinnerLevel = findViewById(R.id.spinnerLevel);
        Spinner spinnerTheme = findViewById(R.id.spinnerTheme);

        SharedPreferences.Editor editor = prefs.edit();
        if (spinnerLanguage != null) editor.putInt("language_pos", spinnerLanguage.getSelectedItemPosition());
        if (spinnerDefaultMode != null) editor.putInt("mode_pos", spinnerDefaultMode.getSelectedItemPosition());
        if (spinnerLevel != null) editor.putInt("level_pos", spinnerLevel.getSelectedItemPosition());
        if (spinnerTheme != null) {
            int themePos = spinnerTheme.getSelectedItemPosition();
            editor.putInt("theme_pos", themePos);
            // Apply theme
            switch (themePos) {
                case 0: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
                case 1: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
                case 2: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
            }
        }
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
