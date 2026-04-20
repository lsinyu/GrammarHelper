package com.example.grammarhelper.util;

import com.example.grammarhelper.model.GrammarError;
import java.util.List;

public class ScoreCalculator {

    public static int calculateGrammarScore(int wordCount, List<GrammarError> errors) {
        if (wordCount == 0) return 100;
        if (errors == null || errors.isEmpty()) return 100;

        int criticalErrors = 0;
        int majorErrors = 0;
        int minorErrors = 0;

        for (GrammarError error : errors) {
            if (error.errorType == null) continue;

            switch (error.errorType) {
                case "Correctness":
                    criticalErrors++;
                    break;
                case "Clarity":
                    majorErrors++;
                    break;
                case "Tone":
                case "Engagement":
                    minorErrors++;
                    break;
                default:
                    minorErrors++;
                    break;
            }
        }

        double leniencyFactor = Math.min(1.5, 1.0 + (wordCount / 200.0));

        double criticalDeduction = criticalErrors * (8.0 / leniencyFactor);
        double majorDeduction = majorErrors * (4.0 / leniencyFactor);
        double minorDeduction = minorErrors * (1.5 / leniencyFactor);

        double totalDeduction = criticalDeduction + majorDeduction + minorDeduction;
        totalDeduction = Math.min(95, totalDeduction);

        int finalScore = (int) Math.round(100 - totalDeduction);

        return Math.max(0, Math.min(100, finalScore));
    }

    public static int calculateClarityScore(int wordCount, List<GrammarError> errors) {
        if (wordCount == 0) return 100;
        if (errors == null || errors.isEmpty()) return 100;

        int clarityIssues = 0;
        int totalIssues = 0;

        for (GrammarError error : errors) {
            totalIssues++;
            if ("Clarity".equals(error.errorType)) {
                clarityIssues++;
            }
        }

        if (totalIssues == 0) return 100;

        double clarityRatio = 1.0 - ((double) clarityIssues / totalIssues);
        int baseScore = Math.min(100, 80 + (wordCount / 20));

        return (int) Math.round(baseScore * clarityRatio);
    }

    public static String getLetterGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 85) return "A";
        if (score >= 80) return "A-";
        if (score >= 75) return "B+";
        if (score >= 70) return "B";
        if (score >= 65) return "B-";
        if (score >= 60) return "C+";
        if (score >= 55) return "C";
        if (score >= 50) return "C-";
        if (score >= 40) return "D";
        return "F";
    }

    public static String getScoreFeedback(int score) {
        if (score >= 90) return "Excellent! Your writing is very polished.";
        if (score >= 80) return "Great job! A few small improvements needed.";
        if (score >= 70) return "Good work! Review the suggestions to improve.";
        if (score >= 60) return "Decent attempt. Focus on the grammar rules shown.";
        if (score >= 50) return "Needs practice. Try our chatbot for help!";
        if (score >= 30) return "Significant improvement needed. Start with basics.";
        return "Let's work together to improve your grammar!";
    }
}