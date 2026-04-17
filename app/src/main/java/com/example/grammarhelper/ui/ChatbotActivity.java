package com.example.grammarhelper.ui;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grammarhelper.R;
import com.example.grammarhelper.adapter.ChatAdapter;
import com.example.grammarhelper.ai.GeminiApiClient;
import com.example.grammarhelper.ai.PromptBuilder;
import com.example.grammarhelper.database.ChatHistoryDAO;
import com.example.grammarhelper.model.ChatMessage;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatbotActivity extends AppCompatActivity implements ChatAdapter.OnQuizSubmitListener {
    private static final String TAG = "ChatbotActivity";
    private static final int SPEECH_REQUEST_CODE = 100;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText chatInputText;
    private ImageButton btnSend, btnClearChat, btnVoice;
    private TextView contextTextView;
    private Chip chipExplain, chipRewrite, chipQuiz;
    private Toolbar toolbar;

    private GeminiApiClient aiClient;
    private ChatHistoryDAO db;
    private String currentContextText = "";
    private boolean isWaitingForResponse = false;
    private boolean isWaitingForRewriteText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        currentContextText = getIntent().getStringExtra("context");
        if (currentContextText == null) currentContextText = "";

        aiClient = new GeminiApiClient();
        db = new ChatHistoryDAO(this);
        db.open();

        initViews();
        loadChatHistory();
        setupListeners();

        if (chatAdapter.getItemCount() == 0) {
            addBotMessage("Hello! I am your AI Grammar Tutor. How can I help you improve your writing today?");
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.chatbotToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("AI Grammar Tutor");
        }

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatInputText = findViewById(R.id.chatInputText);
        btnSend = findViewById(R.id.btnSend);
        btnClearChat = findViewById(R.id.btnClearChat);
        btnVoice = findViewById(R.id.btnVoice);
        contextTextView = findViewById(R.id.contextText);
        chipExplain = findViewById(R.id.chipExplain);
        chipRewrite = findViewById(R.id.chipRewrite);
        chipQuiz = findViewById(R.id.chipQuiz);

        contextTextView.setText("Context: " + (currentContextText.length() > 50 ? currentContextText.substring(0, 47) + "..." : currentContextText));

        chatAdapter = new ChatAdapter(this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage(chatInputText.getText().toString()));
        btnVoice.setOnClickListener(v -> startVoiceInput());
        chipExplain.setOnClickListener(v -> sendMessage("Explain the grammar in this text."));
        chipRewrite.setOnClickListener(v -> {
            addBotMessage("Sure! Please provide the text you would like me to rewrite.");
            isWaitingForRewriteText = true;
            chatInputText.requestFocus();
        });
        chipQuiz.setOnClickListener(v -> sendMessage("Give me a quick grammar quiz based on my context."));

        if (btnClearChat != null) {
            btnClearChat.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                    .setTitle("Clear Chat History")
                    .setMessage("Are you sure you want to clear all chat history?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        db.clearHistory();
                        chatAdapter.setMessages(new java.util.ArrayList<>());
                        addBotMessage("Chat history cleared. How can I help you?");
                        Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                chatInputText.setText(result.get(0));
                sendMessage(result.get(0));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String message) {
        if (message.trim().isEmpty()) return;
        if (isWaitingForResponse) {
            Toast.makeText(this, "Please wait for the current response...", Toast.LENGTH_SHORT).show();
            return;
        }

        chatInputText.setText("");
        addUserMessage(message);

        isWaitingForResponse = true;
        addBotMessage("Thinking...");
        final int typingPosition = chatAdapter.getItemCount() - 1;

        String lowerMessage = message.toLowerCase();
        String prompt;
        
        if (isWaitingForRewriteText) {
            prompt = PromptBuilder.buildRewriteStylesPrompt(message);
            isWaitingForRewriteText = false;
        } else if (lowerMessage.contains("my answers are") || lowerMessage.contains("answer 1:")) {
            String history = getRecentChatHistory(5);
            prompt = PromptBuilder.buildCheckQuizAnswersPrompt(history, message);
        } else if (lowerMessage.contains("explain")) {
            prompt = PromptBuilder.buildExplainErrorPrompt("General", currentContextText, "See below", "Intermediate");
        } else if (lowerMessage.contains("rewrite")) {
            addBotMessage("Sure! Please provide the text you would like me to rewrite.");
            isWaitingForRewriteText = true;
            isWaitingForResponse = false;
            chatAdapter.removeMessageAt(typingPosition);
            return;
        } else if (lowerMessage.contains("quiz")) {
            prompt = PromptBuilder.buildGenerateQuizPrompt("Grammar");
        } else {
            prompt = "You are a helpful AI Grammar Tutor. Context: \"" + currentContextText + "\". Question: " + message;
        }

        aiClient.generateContent(prompt, new GeminiApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    isWaitingForResponse = false;
                    chatAdapter.removeMessageAt(typingPosition);
                    addBotMessage(response);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isWaitingForResponse = false;
                    chatAdapter.removeMessageAt(typingPosition);
                    addBotMessage("⚠️ Error: " + error);
                });
            }
        });
    }

    private String getRecentChatHistory(int limit) {
        List<ChatMessage> messages = chatAdapter.getMessages();
        StringBuilder history = new StringBuilder();
        int start = Math.max(0, messages.size() - limit);
        for (int i = start; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            if (msg.userMessage != null) history.append("User: ").append(msg.userMessage).append("\n");
            if (msg.botResponse != null && !"Thinking...".equals(msg.botResponse)) {
                history.append("AI: ").append(msg.botResponse).append("\n");
            }
        }
        return history.toString();
    }

    @Override
    public void onQuizSubmit(String answers) {
        sendMessage("My answers are: \n" + answers + "\nPlease grade them and explain.");
    }

    private void addUserMessage(String text) {
        ChatMessage msg = new ChatMessage(text, null, currentContextText, getCurrentTimestamp());
        chatAdapter.addMessage(msg);
        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        db.insertChatMessage(msg);
    }

    private void addBotMessage(String text) {
        ChatMessage msg = new ChatMessage(null, text, currentContextText, getCurrentTimestamp());
        chatAdapter.addMessage(msg);
        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        if (!"Thinking...".equals(text)) {
            db.insertChatMessage(msg);
        }
    }

    private void loadChatHistory() {
        List<ChatMessage> history = db.getAllChatHistory();
        chatAdapter.setMessages(history);
        if (history.size() > 0) {
            chatRecyclerView.scrollToPosition(history.size() - 1);
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
