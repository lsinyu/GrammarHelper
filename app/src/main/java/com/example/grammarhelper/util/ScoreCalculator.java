package com.example.grammarhelper.util;

import com.example.grammarhelper.model.GrammarError;
import java.util.List;

public class ScoreCalculator {

    public static int calculateGrammarScore(int wordCount, List<GrammarError> errors) {
        if (wordCount == 0) return 100;

        int errorCount = 0;
        for (GrammarError error : errors) {
            if ("Correctness".equals(error.errorType)) {
                errorCount++;
            }
        }

        // Simple deduction formula: 10 points per error per 100 words
        int deduction = (int) ((errorCount / (float) wordCount) * 1000);
        return Math.max(0, 100 - deduction);
    }

    public static int calculateClarityScore(int wordCount, List<GrammarError> errors) {
        if (wordCount == 0) return 100;

        int clarityIssues = 0;
        for (GrammarError error : errors) {
            if ("Clarity".equals(error.errorType)) {
                clarityIssues++;
            }
        }

        int deduction = (int) ((clarityIssues / (float) wordCount) * 1000);
        return Math.max(0, 100 - deduction);
    }
}
