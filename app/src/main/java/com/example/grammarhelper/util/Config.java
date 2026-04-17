package com.example.grammarhelper.util;

import com.example.grammarhelper.BuildConfig;

public class Config {
    // API key is now fetched securely from local.properties via BuildConfig
    public static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=" + GEMINI_API_KEY;
}
