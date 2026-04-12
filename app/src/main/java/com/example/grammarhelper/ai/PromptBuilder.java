package com.example.grammarhelper.ai;

import org.json.JSONException;
import org.json.JSONObject;

public class PromptBuilder {

    public static String buildGrammarAnalysisPrompt(String text, String contextMode) {
        return "You are a professional grammar analysis engine. " +
                "Analyze the following text and return ONLY a valid JSON array. " +
                "Each object in the array must have: " +
                "\"errorType\": \"Correctness\" | \"Clarity\" | \"Engagement\" | \"Tone\", " +
                "\"errorSubtype\": specific rule (e.g., \"Subject-Verb Agreement\"), " +
                "\"originalText\": the exact problematic text, " +
                "\"suggestion\": the corrected version, " +
                "\"explanation\": one simple sentence explaining why (max 20 words), " +
                "\"positionStart\": character index of error start, " +
                "\"positionEnd\": character index of error end. " +
                "Text to analyze: \"" + text + "\". " +
                "Context mode: " + contextMode;
    }

    public static String buildExplainErrorPrompt(String subtype, String original, String suggestion, String level) {
        return "You are a friendly English grammar tutor for students. " +
                "Explain the following grammar error clearly and simply. " +
                "Use a real-life example. Keep it under 80 words. " +
                "Max difficulty: " + level + ". " +
                "Error type: " + subtype + ". " +
                "Original text: \"" + original + "\". " +
                "Suggested fix: \"" + suggestion + "\".";
    }

    public static String buildRewriteStylesPrompt(String text) {
        return "Rewrite the following text in exactly 3 versions. " +
                "Label each version clearly as: " +
                "PROFESSIONAL: [rewritten text], " +
                "CASUAL: [rewritten text], " +
                "SHORT: [rewritten text — max 60% of original length]. " +
                "Text: \"" + text + "\"";
    }

    public static String buildGenerateQuizPrompt(String subtype) {
        return "Generate exactly 3 multiple-choice grammar questions targeting the error type: \"" + subtype + "\". " +
                "Format each question as: " +
                "Q: [question] " +
                "A) [option] B) [option] C) [option] D) [option] " +
                "Answer: [correct letter] " +
                "Explanation: [one sentence]";
    }

    public static String buildToneDetectionPrompt(String text, String targetMode) {
        return "Analyze the tone of this text relative to the target mode: \"" + targetMode + "\". " +
                "Return ONLY a JSON object: " +
                "{" +
                "\"tone\": \"Academic\" | \"Professional\" | \"Casual\" | \"Friendly\" | \"Assertive\", " +
                "\"formality\": \"High\" | \"Medium\" | \"Low\", " +
                "\"confidence\": \"High\" | \"Medium\" | \"Low\", " +
                "\"isMatch\": boolean (true if matches " + targetMode + ")" +
                "} " +
                "Text: \"" + text + "\"";
    }
}
