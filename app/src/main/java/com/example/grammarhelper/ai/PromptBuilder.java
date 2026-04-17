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
        return "Rewrite the following text in exactly 3 versions: Professional, Casual, and Short. " +
                "Return ONLY a valid JSON object with the following structure: " +
                "{" +
                "\"rewrites\": [" +
                "  {\"title\": \"Professional\", \"content\": \"...\"}," +
                "  {\"title\": \"Casual\", \"content\": \"...\"}," +
                "  {\"title\": \"Short\", \"content\": \"...\"}" +
                "]" +
                "} " +
                "Text: \"" + text + "\"";
    }

    public static String buildGenerateQuizPrompt(String subtype) {
        return "Generate exactly 3 multiple-choice grammar questions targeting: \"" + subtype + "\". " +
                "If subtype is 'Grammar', pick a random common grammar topic. " +
                "IMPORTANT: Use simple English and easy-to-understand words suitable for beginners. " +
                "Return ONLY a valid JSON array of objects. Each object must have: " +
                "\"id\": integer, " +
                "\"question\": the question text, " +
                "\"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"] " +
                "DO NOT provide answers or explanations. DO NOT include any text other than the JSON array.";
    }

    public static String buildGenerateTestPrompt(String history) {
        return "You are an AI Grammar Tutor. Create a comprehensive test with exactly 10 multiple-choice questions. " +
                "Base the questions on the user's recent chat history and common mistakes found here: \n" + history + "\n" +
                "If history is empty or insufficient, generate random beginner-to-intermediate English grammar questions. " +
                "Return ONLY a valid JSON array. Each object MUST include: " +
                "\"id\": integer (1-10), " +
                "\"question\": the question text, " +
                "\"options\": [\"A\", \"B\", \"C\", \"D\"], " +
                "\"correctIndex\": integer (0-3), " +
                "\"explanation\": a brief one-sentence tip. " +
                "Use simple language.";
    }

    public static String buildCheckQuizAnswersPrompt(String history, String userAnswers) {
        return "You are a helpful AI Grammar Tutor. A student has just attempted a quiz. " +
                "Here is the recent conversation history for context: \n" + history + "\n" +
                "The student's answers/input: \"" + userAnswers + "\". " +
                "Please: " +
                "1. Grade the answers and provide a clear SCORE (e.g., 2/3). " +
                "2. Provide the correct answers. " +
                "3. Give a clear explanation and a helpful tip for each question using simple English. " +
                "Keep your response encouraging and educational.";
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
