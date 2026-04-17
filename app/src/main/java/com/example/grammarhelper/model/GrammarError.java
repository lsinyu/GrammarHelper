package com.example.grammarhelper.model;

import com.google.gson.annotations.SerializedName;

public class GrammarError {
    public int id;
    public int sessionId;

    @SerializedName("errorType")
    public String errorType; // "Correctness" | "Clarity" | "Engagement" | "Tone"

    @SerializedName("errorSubtype")
    public String errorSubtype; // e.g., "Subject-Verb Agreement"

    @SerializedName("originalText")
    public String originalText;

    public String suggestion;

    public int wasAccepted; // 1 = fixed, 0 = dismissed

    // For analysis results
    public String explanation;

    @SerializedName("positionStart")
    public int positionStart;

    @SerializedName("positionEnd")
    public int positionEnd;

    public GrammarError() {}

    public GrammarError(String errorType, String errorSubtype, String originalText, String suggestion, String explanation, int positionStart, int positionEnd) {
        this.errorType = errorType;
        this.errorSubtype = errorSubtype;
        this.originalText = originalText;
        this.suggestion = suggestion;
        this.explanation = explanation;
        this.positionStart = positionStart;
        this.positionEnd = positionEnd;
    }
}
