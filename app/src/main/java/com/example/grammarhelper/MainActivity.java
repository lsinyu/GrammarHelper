package com.example.grammarhelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.grammarhelper.ui.LoginActivity;
import com.example.grammarhelper.ui.SmartEditorActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "GrammarHelperPrefs";
    private static final String SETTINGS_PREFS = "grammar_helper_prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_THEME = "theme_pos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme BEFORE super.onCreate
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        applyTheme(prefs.getInt(KEY_THEME, 0));

        super.onCreate(savedInstanceState);

        if (!isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        startActivity(new Intent(this, SmartEditorActivity.class));
        finish();
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

    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}