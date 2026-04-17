package com.example.grammarhelper.ui;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.grammarhelper.R;
import com.example.grammarhelper.ai.GeminiApiClient;
import com.example.grammarhelper.ai.PromptBuilder;
import com.example.grammarhelper.database.ChatHistoryDAO;
import com.example.grammarhelper.database.DatabaseHelper;
import com.example.grammarhelper.model.ChatMessage;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";
    private static final long TEST_DURATION_MS = 30 * 60 * 1000; // 30 minutes

    private TextView timerText, scoreSummary;
    private LinearLayout testContainer, resultLayout, reviewContainer, startScreen;
    private View testScrollView;
    private Button btnSubmit, btnRetake, btnReviewTest, btnHome, btnStartActualTest, btnBackFromStart;
    private GeminiApiClient aiClient;
    private ChatHistoryDAO chatHistoryDAO;
    private DatabaseHelper dbHelper;
    private CountDownTimer countDownTimer;

    private final List<Question> currentQuestions = new ArrayList<>();
    private int attemptCount = 0;
    private int bestScore = -1;
    private boolean isTestInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        aiClient = new GeminiApiClient();
        chatHistoryDAO = new ChatHistoryDAO(this);
        chatHistoryDAO.open();
        dbHelper = new DatabaseHelper(this);

        initViews();
        setupOnBackPressed();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.testToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Learning Evaluation");
        }

        startScreen = findViewById(R.id.startScreen);
        testScrollView = findViewById(R.id.testScrollView);
        timerText = findViewById(R.id.timerText);
        testContainer = findViewById(R.id.testContainer);
        btnSubmit = findViewById(R.id.btnSubmitTest);
        resultLayout = findViewById(R.id.resultLayout);
        scoreSummary = findViewById(R.id.scoreSummary);
        btnRetake = findViewById(R.id.btnRetake);
        btnReviewTest = findViewById(R.id.btnReviewTest);
        btnHome = findViewById(R.id.btnHome);
        reviewContainer = findViewById(R.id.reviewContainer);
        btnStartActualTest = findViewById(R.id.btnStartActualTest);
        btnBackFromStart = findViewById(R.id.btnBackFromStart);

        btnStartActualTest.setOnClickListener(v -> startNewTest());
        btnBackFromStart.setOnClickListener(v -> finish());
        btnHome.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            if (isAllQuestionsAnswered()) {
                submitTest();
            } else {
                Toast.makeText(this, "Please answer all questions before submitting.", Toast.LENGTH_LONG).show();
            }
        });
        
        btnRetake.setOnClickListener(v -> {
            if (attemptCount < 2) {
                startNewTest();
            } else {
                Toast.makeText(this, "No attempts left!", Toast.LENGTH_SHORT).show();
            }
        });

        btnReviewTest.setOnClickListener(v -> {
            if (reviewContainer.getVisibility() == View.VISIBLE) {
                reviewContainer.setVisibility(View.GONE);
                btnReviewTest.setText("Review Questions");
            } else {
                populateReview();
                reviewContainer.setVisibility(View.VISIBLE);
                btnReviewTest.setText("Hide Review");
            }
        });
    }

    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isTestInProgress) {
                    new MaterialAlertDialogBuilder(TestActivity.this)
                        .setTitle("Exit Test?")
                        .setMessage("A test is currently in progress. If you leave now, your attempt will not be saved. Are you sure you want to exit?")
                        .setPositiveButton("Exit", (dialog, which) -> {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startNewTest() {
        attemptCount++;
        isTestInProgress = false; 
        
        startScreen.setVisibility(View.GONE);
        testScrollView.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        testContainer.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        reviewContainer.setVisibility(View.GONE);
        reviewContainer.removeAllViews();
        btnReviewTest.setText("Review Questions");
        
        Toast.makeText(this, "Generating your personalized test...", Toast.LENGTH_LONG).show();
        
        String history = getRecentHistory();
        aiClient.generateContent(PromptBuilder.buildGenerateTestPrompt(history), new GeminiApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    parseAndDisplayTest(response);
                    startTimer();
                    isTestInProgress = true;
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(TestActivity.this, "AI Error: " + error, Toast.LENGTH_LONG).show();
                    startScreen.setVisibility(View.VISIBLE);
                    testScrollView.setVisibility(View.GONE);
                });
            }
        });
    }

    private String getRecentHistory() {
        List<ChatMessage> historyList = chatHistoryDAO.getAllChatHistory();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = historyList.size() - 1; i >= 0 && count < 10; i--) {
            if (historyList.get(i).userMessage != null)
                sb.append("User: ").append(historyList.get(i).userMessage).append("\n");
            if (historyList.get(i).botResponse != null)
                sb.append("AI: ").append(historyList.get(i).botResponse).append("\n");
            count++;
        }
        return sb.toString();
    }

    private void parseAndDisplayTest(String jsonResponse) {
        try {
            int startIdx = jsonResponse.indexOf("[");
            int endIdx = jsonResponse.lastIndexOf("]") + 1;
            if (startIdx == -1 || endIdx == -1) {
                Log.e(TAG, "Invalid JSON response");
                return;
            }
            JSONArray array = new JSONArray(jsonResponse.substring(startIdx, endIdx));
            currentQuestions.clear();
            testContainer.removeAllViews();
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Question q = new Question();
                q.id = obj.getInt("id");
                q.questionText = obj.getString("question");
                q.correctIndex = obj.getInt("correctIndex");
                q.explanation = obj.getString("explanation");
                
                JSONArray optionsArray = obj.getJSONArray("options");
                q.options = new String[optionsArray.length()];
                for (int j = 0; j < optionsArray.length(); j++) q.options[j] = optionsArray.getString(j);
                
                currentQuestions.add(q);
                addQuestionToUI(q, i + 1);
            }
            
            testContainer.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            
        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
        }
    }

    private void addQuestionToUI(Question q, int num) {
        TextView tv = new TextView(this);
        tv.setText(num + ". " + q.questionText);
        tv.setPadding(0, 32, 0, 8);
        tv.setTextSize(16f); 
        testContainer.addView(tv);

        RadioGroup rg = new RadioGroup(this);
        q.radioGroup = rg;
        for (int i = 0; i < q.options.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(q.options[i]);
            rb.setId(i);
            rg.addView(rb);
        }
        testContainer.addView(rg);
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(TEST_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = (millisUntilFinished / 1000) / 60;
                long sec = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format(Locale.getDefault(), "Time Left: %02d:%02d", min, sec));
            }

            @Override
            public void onFinish() {
                timerText.setText("Time Up!");
                submitTest();
            }
        }.start();
    }

    private boolean isAllQuestionsAnswered() {
        if (currentQuestions.isEmpty()) return false;
        for (Question q : currentQuestions) {
            if (q.radioGroup == null || q.radioGroup.getCheckedRadioButtonId() == -1) {
                return false;
            }
        }
        return true;
    }

    private void submitTest() {
        if (countDownTimer != null) countDownTimer.cancel();
        isTestInProgress = false;
        
        int score = 0;
        for (Question q : currentQuestions) {
            int selectedId = q.radioGroup.getCheckedRadioButtonId();
            q.userSelectedIndex = selectedId;
            if (selectedId == q.correctIndex) {
                score++;
            }
        }
        
        if (score > bestScore) bestScore = score;
        
        showResults(score);
        saveToDatabase(score, attemptCount);
    }

    private void showResults(int score) {
        testContainer.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        
        scoreSummary.setText("Current Attempt: " + score + "/10\nBest Score: " + bestScore + "/10");
        
        btnRetake.setEnabled(attemptCount < 2);
        if (attemptCount >= 2) {
             btnRetake.setText("Max attempts reached");
        }
    }

    private void populateReview() {
        reviewContainer.removeAllViews();
        for (int i = 0; i < currentQuestions.size(); i++) {
            Question q = currentQuestions.get(i);
            
            TextView qText = new TextView(this);
            qText.setText((i + 1) + ". " + q.questionText);
            qText.setTextSize(16f);
            qText.setPadding(0, 16, 0, 8);
            qText.setTextColor(Color.BLACK);
            reviewContainer.addView(qText);

            TextView ansText = new TextView(this);
            String userAns = q.userSelectedIndex == -1 ? "None" : q.options[q.userSelectedIndex];
            String correctAns = q.options[q.correctIndex];
            
            ansText.setText("Your Answer: " + userAns + "\nCorrect Answer: " + correctAns);
            ansText.setTextColor(q.userSelectedIndex == q.correctIndex ? Color.parseColor("#2E7D32") : Color.RED);
            reviewContainer.addView(ansText);

            TextView explText = new TextView(this);
            explText.setText("Explanation: " + q.explanation);
            explText.setPadding(0, 4, 0, 16);
            explText.setTextColor(Color.DKGRAY);
            reviewContainer.addView(explText);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
            divider.setBackgroundColor(Color.LTGRAY);
            reviewContainer.addView(divider);
        }
    }

    private void saveToDatabase(int score, int attempt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TEST_SCORE, score);
        values.put(DatabaseHelper.COL_TEST_ATTEMPT, attempt);
        values.put(DatabaseHelper.COL_TEST_TIMESTAMP, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.insert(DatabaseHelper.TABLE_TEST_RESULTS, null, values);
        
        getSharedPreferences("GrammarHelperPrefs", MODE_PRIVATE).edit().putInt("best_test_score", bestScore).apply();
    }

    static class Question {
        int id;
        String questionText;
        String[] options;
        int correctIndex;
        String explanation;
        RadioGroup radioGroup;
        int userSelectedIndex = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatHistoryDAO != null) chatHistoryDAO.close();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
