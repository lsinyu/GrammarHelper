package com.example.grammarhelper.model;

public class ChatMessage {
    public int id;
    public String userMessage;
    public String botResponse;
    public String contextText;
    public String timestamp;

    public ChatMessage() {}

    public ChatMessage(String userMessage, String botResponse, String contextText, String timestamp) {
        this.userMessage = userMessage;
        this.botResponse = botResponse;
        this.contextText = contextText;
        this.timestamp = timestamp;
    }
}
