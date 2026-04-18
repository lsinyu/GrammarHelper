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
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign-In
        // IMPORTANT: Use the WEB CLIENT ID here, not the Android Client ID
// LoginActivity.java around line 43
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("344775839458-o9mfuf7q4m1ki2npnnj0q5hl7e8ic6fr.apps.googleusercontent.com")
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        Button btnGuestLogin = findViewById(R.id.btnGuestLogin);

        if (!isLoggedIn()) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Welcome to Grammar Helper")
                .setMessage("To provide a personalized experience and save your progress, please link your Google account.")
                .setPositiveButton("Got it", null)
                .show();
        }

        btnGoogleLogin.setOnClickListener(v -> signIn());

        if (btnGuestLogin != null) {
            btnGuestLogin.setOnClickListener(v -> {
                setLoggedIn(true);
                navigateToMain();
            });
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
            String errorMsg = e.getMessage();
            Log.e(TAG, "Google Sign-In failed. Status Code: " + statusCode + ", Message: " + errorMsg);
            
            if (statusCode == 10 || statusCode == 12500 || statusCode == 7) {
                // If you are still seeing this, the requestIdToken or SHA-1 is definitely wrong.
                Toast.makeText(this, "Configuration Error (Code " + statusCode + "). Please check Web Client ID and Test Users in Google Console.", Toast.LENGTH_LONG).show();
                setLoggedIn(true);
                navigateToMain();
            } else {
                Toast.makeText(this, "Sign-in failed (Code: " + statusCode + ")", Toast.LENGTH_SHORT).show();
            }
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
