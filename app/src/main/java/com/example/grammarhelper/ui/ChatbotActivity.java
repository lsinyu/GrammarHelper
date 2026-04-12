package com.example.grammarhelper.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatbotActivity extends AppCompatActivity {
    private static final String TAG = "ChatbotActivity";

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText chatInputText;
    private ImageButton btnSend, btnClearChat;
    private TextView contextTextView;
    private Chip chipExplain, chipRewrite, chipQuiz;
    private Toolbar toolbar;

    private GeminiApiClient aiClient;
    private ChatHistoryDAO db;
    private String currentContextText = "";
    private boolean isWaitingForResponse = false;

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

        // Initial greeting if history is empty
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
        contextTextView = findViewById(R.id.contextText);
        chipExplain = findViewById(R.id.chipExplain);
        chipRewrite = findViewById(R.id.chipRewrite);
        chipQuiz = findViewById(R.id.chipQuiz);

        contextTextView.setText("Context: " + (currentContextText.length() > 50 ? currentContextText.substring(0, 47) + "..." : currentContextText));

        chatAdapter = new ChatAdapter();
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage(chatInputText.getText().toString()));

        chipExplain.setOnClickListener(v -> sendMessage("Explain the grammar in this text."));
        chipRewrite.setOnClickListener(v -> sendMessage("Can you rewrite this text in different styles?"));
        chipQuiz.setOnClickListener(v -> sendMessage("Give me a quick grammar quiz based on my context."));

        // Clear chat history button
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

        // Show typing indicator
        isWaitingForResponse = true;
        addBotMessage("Thinking...");
        final int typingPosition = chatAdapter.getItemCount() - 1;

        // Build the prompt based on message content (case-insensitive matching)
        String lowerMessage = message.toLowerCase();
        String prompt;
        if (lowerMessage.contains("explain")) {
            prompt = PromptBuilder.buildExplainErrorPrompt("General", currentContextText, "See below", "Intermediate");
        } else if (lowerMessage.contains("rewrite")) {
            prompt = PromptBuilder.buildRewriteStylesPrompt(currentContextText);
        } else if (lowerMessage.contains("quiz")) {
            prompt = PromptBuilder.buildGenerateQuizPrompt("Grammar");
        } else {
            // For free-form messages, include context in the prompt
            if (!currentContextText.isEmpty()) {
                prompt = "You are a helpful AI Grammar Tutor. The student is working with the following text: \"" 
                    + currentContextText + "\"\n\nStudent's question: " + message 
                    + "\n\nPlease provide a helpful, clear response about grammar, writing, or the text above.";
            } else {
                prompt = "You are a helpful AI Grammar Tutor. Student's question: " + message 
                    + "\n\nPlease provide a helpful, clear response about grammar and writing.";
            }
        }

        Log.d(TAG, "Sending message to AI: " + message);

        aiClient.generateContent(prompt, new GeminiApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "AI response received (length=" + response.length() + ")");
                runOnUiThread(() -> {
                    isWaitingForResponse = false;
                    chatAdapter.removeMessageAt(typingPosition);
                    addBotMessage(response);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "AI error: " + error);
                runOnUiThread(() -> {
                    isWaitingForResponse = false;
                    chatAdapter.removeMessageAt(typingPosition);
                    addBotMessage("⚠️ Sorry, I couldn't process your request. Error: " + error);
                    Toast.makeText(ChatbotActivity.this, "AI error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
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
