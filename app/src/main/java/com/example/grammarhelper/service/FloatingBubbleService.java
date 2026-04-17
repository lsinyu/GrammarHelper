package com.example.grammarhelper.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import com.example.grammarhelper.R;
import com.example.grammarhelper.ai.GeminiApiClient;
import com.example.grammarhelper.ai.GrammarResponseParser;
import com.example.grammarhelper.ai.PromptBuilder;
import com.example.grammarhelper.model.GrammarError;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grammarhelper.adapter.ErrorCardAdapter;
import com.example.grammarhelper.accessibility.GrammarAccessibilityService;

public class FloatingBubbleService extends Service {

    private WindowManager windowManager;
    private View bubbleView, panelView;
    private WindowManager.LayoutParams bubbleParams, panelParams;

    private GeminiApiClient aiClient;
    private GrammarResponseParser parser;
    private List<GrammarError> currentErrors = new ArrayList<>();
    private String currentText = "";
    private Handler analysisHandler = new Handler(Looper.getMainLooper());
    private Runnable analysisRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, createNotification());
        aiClient = new GeminiApiClient();
        parser = new GrammarResponseParser();
        initBubble();
    }

    private Notification createNotification() {
        String channelId = "floating_bubble_service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Floating Bubble", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("GrammarHelper Active")
                .setContentText("Listening for text...")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .build();
    }

    private void initBubble() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        bubbleView = LayoutInflater.from(this).inflate(R.layout.overlay_floating_bubble, null);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;

        bubbleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = 0;
        bubbleParams.y = 100;

        setupDrag(bubbleView, bubbleParams);

        bubbleView.setOnClickListener(v -> togglePanel());
        windowManager.addView(bubbleView, bubbleParams);
    }

    private void setupDrag(final View view, final WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(event.getRawX() - initialTouchX) < 10 && Math.abs(event.getRawY() - initialTouchY) < 10) {
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void togglePanel() {
        if (panelView != null) {
            windowManager.removeView(panelView);
            panelView = null;
        } else {
            showPanel();
        }
    }

    private void showPanel() {
        panelView = LayoutInflater.from(this).inflate(R.layout.overlay_suggestion_panel, null);
        panelParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.BOTTOM;

        Button btnDismissAll = panelView.findViewById(R.id.btn_dismiss_all);
        Button btnFixAll = panelView.findViewById(R.id.btn_fix_all);
        RecyclerView suggestionRecycler = panelView.findViewById(R.id.suggestion_recycler);

        btnDismissAll.setOnClickListener(v -> togglePanel());

        // Setup RecyclerView
        ErrorCardAdapter adapter = new ErrorCardAdapter(this, currentErrors, new ErrorCardAdapter.OnErrorActionListener() {
            @Override
            public void onFixClicked(GrammarError errorToFix) {
                applySingleFix(errorToFix);
            }
        });
        suggestionRecycler.setLayoutManager(new LinearLayoutManager(this));
        suggestionRecycler.setAdapter(adapter);

        btnFixAll.setOnClickListener(v -> {
            applyFixAll();
            togglePanel();
        });

        windowManager.addView(panelView, panelParams);
    }

    private void applyFixAll() {
        if (currentErrors.isEmpty() || currentText == null) return;

        String fixedText = currentText;
        // Sort errors in reverse order of positionStart so replacing text doesn't mess up subsequent indices
        List<GrammarError> sortedErrors = new ArrayList<>(currentErrors);
        sortedErrors.sort((e1, e2) -> Integer.compare(e2.positionStart, e1.positionStart));

        for (GrammarError error : sortedErrors) {
            if (error.positionStart >= 0 && error.positionEnd <= fixedText.length()) {
                fixedText = fixedText.substring(0, error.positionStart) +
                        error.suggestion +
                        fixedText.substring(error.positionEnd);
            }
        }

        // Inject back to accessibility service
        GrammarAccessibilityService accessibilityService = GrammarAccessibilityService.getInstance();
        if (accessibilityService != null) {
            accessibilityService.injectText(fixedText);
        }

        currentErrors.clear();
        updateBubbleUI();
    }

    private void applySingleFix(GrammarError error) {
        if (currentText == null) return;


        if (error.positionStart >= 0 && error.positionEnd <= currentText.length()) {
            currentText = currentText.substring(0, error.positionStart) +
                    error.suggestion +
                    currentText.substring(error.positionEnd);
        }


        com.example.grammarhelper.accessibility.GrammarAccessibilityService accessibilityService = com.example.grammarhelper.accessibility.GrammarAccessibilityService.getInstance();
        if (accessibilityService != null) {
            accessibilityService.injectText(currentText);
        }

        togglePanel();

        currentErrors.clear();
        updateBubbleUI();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ANALYZE_TEXT".equals(intent.getAction())) {
            String text = intent.getStringExtra("text");
            if (text != null && !text.equals(currentText)) {
                currentText = text;
                scheduleAnalysis();
            }
        }
        return START_STICKY;
    }

    private void scheduleAnalysis() {
        if (analysisRunnable != null) analysisHandler.removeCallbacks(analysisRunnable);
        analysisRunnable = this::performAnalysis;
        analysisHandler.postDelayed(analysisRunnable, 1000);
    }

    private void performAnalysis() {
        aiClient.generateContent(PromptBuilder.buildGrammarAnalysisPrompt(currentText, "Professional"), new GeminiApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                currentErrors = parser.parseGrammarErrors(response);
                updateBubbleUI();
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void updateBubbleUI() {
        new Handler(Looper.getMainLooper()).post(() -> {
            View icon = bubbleView.findViewById(R.id.bubble_icon);
            TextView badge = bubbleView.findViewById(R.id.error_badge);
            if (currentErrors.isEmpty()) {
                icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_green)));
                badge.setVisibility(View.GONE);
            } else {
                icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error_red)));
                badge.setVisibility(View.VISIBLE);
                badge.setText(String.valueOf(currentErrors.size()));
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bubbleView != null) windowManager.removeView(bubbleView);
        if (panelView != null) windowManager.removeView(panelView);
    }
}
