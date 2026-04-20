package com.example.grammarhelper.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.example.grammarhelper.R;
import com.example.grammarhelper.ai.GeminiApiClient;
import com.example.grammarhelper.ai.GrammarResponseParser;
import com.example.grammarhelper.ai.PromptBuilder;
import com.example.grammarhelper.database.ErrorLogDAO;
import com.example.grammarhelper.database.SessionDAO;
import com.example.grammarhelper.model.GrammarError;
import com.example.grammarhelper.model.Session;
import com.example.grammarhelper.util.NotificationHelper;
import com.example.grammarhelper.util.ScoreCalculator;
import com.example.grammarhelper.util.TextHighlighter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SmartEditorActivity extends AppCompatActivity {

    private static final String TAG = "SmartEditorActivity";
    private static final String SETTINGS_PREFS = "grammar_helper_prefs";
    private static final String KEY_THEME = "theme_pos";

    private EditText editorText;
    private TextView scoreText, errorCountText, wordCountText, toneStatusText;
    private ChipGroup modeChipGroup;
    private FloatingActionButton chatbotFab;
    private ImageButton btnClear, btnCopy, btnShare, btnReviewPanel, btnSettings;
    private BottomNavigationView bottomNav;

    private GeminiApiClient aiClient;
    private GrammarResponseParser parser;
    private Handler analysisHandler = new Handler(Looper.getMainLooper());
    private Runnable analysisRunnable;
    private boolean isUpdatingText = false;

    private String currentContextMode = "Professional";
    private List<GrammarError> currentErrors = new ArrayList<>();
    private ErrorLogDAO errorLogDAO;
    private SessionDAO sessionDAO;
    private androidx.activity.result.ActivityResultLauncher<Intent> errorReviewLauncher;
    private List<GrammarError> resolvedErrors = new ArrayList<>();
    private String lastSavedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme BEFORE super.onCreate
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        applyTheme(prefs.getInt(KEY_THEME, 0));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_editor);

        aiClient = new GeminiApiClient();
        parser = new GrammarResponseParser();
        errorLogDAO = new ErrorLogDAO(this);
        sessionDAO = new SessionDAO(this);
        errorLogDAO.open();
        sessionDAO.open();
        errorReviewLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Extract the fixed errors sent back from ErrorReviewActivity
                        ArrayList<GrammarError> returnedErrors = (ArrayList<GrammarError>) result.getData().getSerializableExtra("updated_errors");
                        if (returnedErrors != null) {
                            applyBatchFixes(returnedErrors);
                        }
                    }
                }
        );

        initViews();
        setupListeners();
        setupBottomNavigation();

        NotificationHelper.scheduleDailyTip(this, 8, 0);
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
        editorText = findViewById(R.id.smartEditText);
        scoreText = findViewById(R.id.scoreText);
        errorCountText = findViewById(R.id.errorCountText);
        wordCountText = findViewById(R.id.wordCountText);
        toneStatusText = findViewById(R.id.toneStatusText);
        modeChipGroup = findViewById(R.id.modeChipGroup);
        chatbotFab = findViewById(R.id.chatbotFab);
        btnClear = findViewById(R.id.btnClear);
        btnCopy = findViewById(R.id.btnCopy);
        btnShare = findViewById(R.id.btnShare);
        btnReviewPanel = findViewById(R.id.btnReviewPanel);
        bottomNav = findViewById(R.id.bottomNavigation);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setupListeners() {
        editorText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdatingText) {
                    // Reset UI immediately while waiting for analysis
                    TextHighlighter.clearHighlights(editorText.getText());
                    errorCountText.setText("Analyzing...");
                    scheduleAnalysis();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingText) {
                    updateWordCount(s.toString());
                }
            }
        });

        modeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAcademic) currentContextMode = "Academic";
            else if (checkedId == R.id.chipProfessional) currentContextMode = "Professional";
            else if (checkedId == R.id.chipCasual) currentContextMode = "Casual";

            toneStatusText.setText("Tone: Analyzing for " + currentContextMode + "...");
            scheduleAnalysis();
        });

        chatbotFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatbotActivity.class);
            intent.putExtra("context", editorText.getText().toString());
            startActivity(intent);
        });

        btnClear.setOnClickListener(v -> {
            saveCurrentSessionToDb();
            editorText.setText("");
            currentErrors.clear();
            scoreText.setText("Score: 100");
            errorCountText.setText("✅ No Errors");
            toneStatusText.setText("Tone: Ready to analyze");
            TextHighlighter.clearHighlights(editorText.getText());
        });

        btnCopy.setOnClickListener(v -> {
            String text = editorText.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("GrammarHelper Text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            String text = editorText.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(shareIntent, "Share your text via"));
        });

        btnReviewPanel.setOnClickListener(v -> {
            Intent intent = new Intent(this, ErrorReviewActivity.class);
            intent.putExtra("errors_list", new ArrayList<>(currentErrors));
            errorReviewLauncher.launch(intent);
        });

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
            });
        }

        editorText.setOnClickListener(v -> {
            int pos = editorText.getSelectionStart();
            for (GrammarError error : currentErrors) {
                if (pos >= error.positionStart && pos <= error.positionEnd) {
                    showSuggestionPopup(error);
                    return;
                }
            }
        });
    }

    private void setupBottomNavigation() {
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_editor);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_editor) return true;
            else if (id == R.id.nav_chat) {
                Intent intent = new Intent(this, ChatbotActivity.class);
                intent.putExtra("context", editorText.getText().toString());
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_errors) {
                Intent intent = new Intent(this, ErrorReviewActivity.class);
                ArrayList<GrammarError> errorsToPass = currentErrors != null ? new ArrayList<>(currentErrors) : new ArrayList<>();
                intent.putExtra("errors_list", errorsToPass);
                errorReviewLauncher.launch(intent);
                return true;
            } else if (id == R.id.nav_test) {
                startActivity(new Intent(this, TestActivity.class));
                return true;
            } else if (id == R.id.nav_dashboard) {
                saveCurrentSessionToDb();
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showSuggestionPopup(GrammarError error) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        String title = "❌ " + (error.errorType != null ? error.errorType : "Grammar Error");
        String message = "\"" + error.originalText + "\" → \"" + error.suggestion + "\"\n\n"
                + (error.errorSubtype != null ? "(" + error.errorSubtype + ")\n\n" : "")
                + (error.explanation != null ? error.explanation : "");
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("✅ Fix", (dialog, which) -> applyFix(error))
                .setNegativeButton("❌ Ignore", null)
                .setNeutralButton("? Why", (dialog, which) -> {
                    Intent intent = new Intent(this, ChatbotActivity.class);
                    intent.putExtra("context", error.originalText);
                    startActivity(intent);
                })
                .show();
    }

    private void applyFix(GrammarError error) {
        isUpdatingText = true;
        android.text.Editable editable = editorText.getText();
        String currentText = editable.toString();

        int start = error.positionStart;
        int end = error.positionEnd;

        boolean exactMatch = false;
        if (start >= 0 && end <= currentText.length() && start < end) {
            if (currentText.substring(start, end).equals(error.originalText)) {
                exactMatch = true;
            }
        }

        if (!exactMatch) {
            int fallbackStart = currentText.indexOf(error.originalText);
            if (fallbackStart != -1) {
                start = fallbackStart;
                end = start + error.originalText.length();
            }
        }

        if (start >= 0 && end <= editable.length() && start < end) {
            editable.replace(start, end, error.suggestion);
            error.wasAccepted = 1;
            resolvedErrors.add(error);
        }

        TextHighlighter.clearHighlights(editable);
        isUpdatingText = false;
        errorCountText.setText("Analyzing...");
        scheduleAnalysis();
    }

    private void updateWordCount(String text) {
        String trimmed = text.trim();
        int count = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        wordCountText.setText("📝 " + count + " Words");
    }

    private void scheduleAnalysis() {
        if (analysisRunnable != null) analysisHandler.removeCallbacks(analysisRunnable);
        analysisRunnable = () -> performAnalysis();
        analysisHandler.postDelayed(analysisRunnable, 1500);
    }

    private void performAnalysis() {
        final String text = editorText.getText().toString();
        if (text.trim().isEmpty()) {
            runOnUiThread(() -> {
                currentErrors.clear();
                errorCountText.setText("✅ No Errors");
                scoreText.setText("Score: 100");
                TextHighlighter.clearHighlights(editorText.getText());
            });
            return;
        }

        aiClient.generateContent(PromptBuilder.buildGrammarAnalysisPrompt(text, currentContextMode), new GeminiApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                currentErrors = parser.parseGrammarErrors(response);
                updateUIWithResult(text, currentErrors);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (error.contains("429")) {
                        errorCountText.setText("⚠️ Slow down...");
                    } else {
                        errorCountText.setText("❌ Offline");
                    }
                    TextHighlighter.clearHighlights(editorText.getText());
                });
            }
        });

        performToneDetection(text);
    }

    private void performToneDetection(String text) {
        aiClient.generateContent(PromptBuilder.buildToneDetectionPrompt(text, currentContextMode), new GeminiApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        String jsonStr = response.trim();
                        int start = jsonStr.indexOf("{");
                        int end = jsonStr.lastIndexOf("}") + 1;
                        if (start >= 0 && end > start) {
                            jsonStr = jsonStr.substring(start, end);
                            JSONObject toneJson = new JSONObject(jsonStr);
                            String tone = toneJson.optString("tone", "Unknown");
                            String formality = toneJson.optString("formality", "");
                            boolean isMatch = toneJson.optBoolean("isMatch", false);
                            String toneEmoji = getToneEmoji(tone);
                            String status = isMatch ? "✅ Match" : "⚠️ Needs work";
                            toneStatusText.setText("Tone: " + toneEmoji + " " + tone + " | " + formality + " | " + status);
                        }
                    } catch (JSONException e) {
                        toneStatusText.setText("Tone: " + currentContextMode + " | ✅ Analyzed");
                    }
                });
            }

            @Override
            public void onError(String error) {}
        });
    }

    private String getToneEmoji(String tone) {
        if (tone == null) return "📝";
        switch (tone.toLowerCase()) {
            case "academic": return "📘";
            case "formal": return "💼";
            case "professional": return "💼";
            case "friendly": return "😊";
            case "assertive": return "💪";
            case "blunt": return "🔨";
            case "casual": return "😎";
            case "optimistic": return "☀️";
            default: return "📝";
        }
    }

    private void updateUIWithResult(String text, List<GrammarError> errors) {
        runOnUiThread(() -> {
            String currentText = editorText.getText().toString();

            if (currentText.equals(text)) {
                isUpdatingText = true;
                TextHighlighter.applyHighlights(this, editorText.getText(), errors);
                isUpdatingText = false;

                int wordCount = text.trim().isEmpty() ? 0 : text.split("\\s+").length;
                int grammarScore = ScoreCalculator.calculateGrammarScore(wordCount, errors);
                String letterGrade = ScoreCalculator.getLetterGrade(grammarScore);
                String feedback = ScoreCalculator.getScoreFeedback(grammarScore);

                scoreText.setText("Score: " + grammarScore + " (" + letterGrade + ")");

                if (grammarScore >= 95 && errors.size() > 0) {
                    errorCountText.setText("✨ " + errors.size() + " minor suggestions");
                    errorCountText.setTextColor(ContextCompat.getColor(this, R.color.success_green));
                } else if (errors.size() > 0) {
                    errorCountText.setText("❌ " + errors.size() + " Errors");
                    errorCountText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
                } else {
                    errorCountText.setText("✅ No Errors");
                    errorCountText.setTextColor(ContextCompat.getColor(this, R.color.success_green));
                }

                if (grammarScore < 60) {
                    Toast.makeText(SmartEditorActivity.this, feedback, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveCurrentSessionToDb() {
        String text = editorText.getText().toString().trim();

        if (text.isEmpty() || currentErrors == null || text.equals(lastSavedText)) return;

        int wordCount = text.split("\\s+").length;
        int grammarScore = ScoreCalculator.calculateGrammarScore(wordCount, currentErrors);
        int clarityScore = ScoreCalculator.calculateClarityScore(wordCount, currentErrors);

        Session session = new Session();
        session.textContent = text;
        session.grammarScore = grammarScore;
        session.clarityScore = clarityScore;
        session.contextMode = currentContextMode;
        session.toneDetected = currentContextMode;
        session.wordCount = wordCount;
        session.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        long sessionId = sessionDAO.insertSession(session);

        for (GrammarError error : currentErrors) {
            error.sessionId = (int) sessionId;
            errorLogDAO.insertErrorLog(error);
        }

        for (GrammarError error : resolvedErrors) {
            error.sessionId = (int) sessionId;
            errorLogDAO.insertErrorLog(error);
        }

        resolvedErrors.clear();
        lastSavedText = text;
    }

    private void applyBatchFixes(List<GrammarError> errors) {
        isUpdatingText = true;
        android.text.Editable editable = editorText.getText();
        String currentText = editable.toString();

        java.util.Collections.sort(errors, (e1, e2) -> Integer.compare(e2.positionStart, e1.positionStart));

        for (GrammarError error : errors) {
            if (error.wasAccepted == 1) {
                int start = error.positionStart;
                int end = error.positionEnd;

                boolean exactMatch = false;
                if (start >= 0 && end <= currentText.length() && start < end) {
                    if (currentText.substring(start, end).equals(error.originalText)) {
                        exactMatch = true;
                    }
                }

                if (!exactMatch) {
                    int fallbackStart = currentText.indexOf(error.originalText);
                    if (fallbackStart != -1) {
                        start = fallbackStart;
                        end = start + error.originalText.length();
                    }
                }

                if (start >= 0 && end <= editable.length() && start < end) {
                    editable.replace(start, end, error.suggestion);
                    resolvedErrors.add(error);
                    currentText = editable.toString();
                }
            }
        }

        TextHighlighter.clearHighlights(editable);
        isUpdatingText = false;
        errorCountText.setText("Analyzing...");
        scheduleAnalysis();
    }

    @Override
    protected void onDestroy() {
        saveCurrentSessionToDb();
        if (errorLogDAO != null) errorLogDAO.close();
        if (sessionDAO != null) sessionDAO.close();
        super.onDestroy();
    }
}