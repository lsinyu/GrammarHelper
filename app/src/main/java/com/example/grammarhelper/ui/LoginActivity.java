package com.example.grammarhelper.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.grammarhelper.MainActivity;
import com.example.grammarhelper.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final String PREFS_NAME = "GrammarHelperPrefs";
    private static final String SETTINGS_PREFS = "grammar_helper_prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_THEME = "theme_pos";

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme BEFORE super.onCreate
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        applyTheme(prefs.getInt(KEY_THEME, 0));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("YOUR_WEB_CLIENT_ID")
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        Button btnGuestLogin = findViewById(R.id.btnGuestLogin);

        btnGoogleLogin.setOnClickListener(v -> signIn());

        if (btnGuestLogin != null) {
            btnGuestLogin.setOnClickListener(v -> {
                setLoggedIn(true);
                navigateToMain();
            });
        }
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

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String displayName = account.getDisplayName();
                Toast.makeText(this, "Welcome, " + displayName, Toast.LENGTH_SHORT).show();
                setLoggedIn(true);
                navigateToMain();
            }
        } catch (ApiException e) {
            int statusCode = e.getStatusCode();
            Log.e(TAG, "Google Sign-In failed. Status Code: " + statusCode);
            Toast.makeText(this, "Sign-in failed. Continue as guest.", Toast.LENGTH_SHORT).show();
            setLoggedIn(true);
            navigateToMain();
        }
    }

    private void navigateToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void setLoggedIn(boolean value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply();
    }
}