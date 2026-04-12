package com.example.grammarhelper.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.grammarhelper.ai.GeminiApiClient;
import com.example.grammarhelper.ai.GrammarResponseParser;
import com.example.grammarhelper.ai.PromptBuilder;
import com.example.grammarhelper.model.GrammarError;
import java.util.List;

/**
 * Background grammar analysis worker service.
 * This service runs in the background and performs grammar analysis
 * when text is submitted for analysis from the editor or floating bubble.
 */
public class GrammarAnalysisService extends Service {

    private static final String TAG = "GrammarAnalysisService";
    public static final String ACTION_ANALYZE = "com.example.grammarhelper.ACTION_ANALYZE";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_CONTEXT_MODE = "context_mode";
    public static final String ACTION_RESULT = "com.example.grammarhelper.ACTION_ANALYSIS_RESULT";
    public static final String EXTRA_RESULT_JSON = "result_json";

    private GeminiApiClient aiClient;
    private GrammarResponseParser parser;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        aiClient = new GeminiApiClient();
        parser = new GrammarResponseParser();
        Log.d(TAG, "GrammarAnalysisService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_ANALYZE.equals(intent.getAction())) {
            String text = intent.getStringExtra(EXTRA_TEXT);
            String contextMode = intent.getStringExtra(EXTRA_CONTEXT_MODE);
            if (contextMode == null) contextMode = "Professional";

            if (text != null && !text.trim().isEmpty()) {
                performAnalysis(text, contextMode);
            }
        }
        return START_NOT_STICKY;
    }

    private void performAnalysis(String text, String contextMode) {
        Log.d(TAG, "Starting analysis for text length: " + text.length());

        String prompt = PromptBuilder.buildGrammarAnalysisPrompt(text, contextMode);

        aiClient.generateContent(prompt, new GeminiApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Analysis complete, broadcasting result");
                List<GrammarError> errors = parser.parseGrammarErrors(response);

                // Broadcast result back
                Intent resultIntent = new Intent(ACTION_RESULT);
                resultIntent.putExtra(EXTRA_RESULT_JSON, response);
                sendBroadcast(resultIntent);

                stopSelf();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Analysis error: " + error);
                stopSelf();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "GrammarAnalysisService destroyed");
    }
}
