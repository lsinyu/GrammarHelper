package com.example.grammarhelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.grammarhelper.ui.LoginActivity;
import com.example.grammarhelper.ui.SmartEditorActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "GrammarHelperPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if logged in. If not, redirect to Login.
        if (!isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // If logged in, go directly to the SmartEditor (homepage)
        startActivity(new Intent(this, SmartEditorActivity.class));
        finish();
    }

    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}
