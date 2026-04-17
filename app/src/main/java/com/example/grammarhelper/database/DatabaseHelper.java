package com.example.grammarhelper.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "grammar_helper.db";
    private static final int    DB_VERSION = 2; // Incremented version

    // Table 1: user_sessions
    public static final String TABLE_SESSIONS = "user_sessions";
    public static final String COL_SESSION_ID = "session_id";
    public static final String COL_TEXT_CONTENT = "text_content";
    public static final String COL_GRAMMAR_SCORE = "grammar_score";
    public static final String COL_CLARITY_SCORE = "clarity_score";
    public static final String COL_CONTEXT_MODE = "context_mode";
    public static final String COL_TONE_DETECTED = "tone_detected";
    public static final String COL_WORD_COUNT = "word_count";
    public static final String COL_TIMESTAMP = "timestamp";

    // Table 2: error_log
    public static final String TABLE_ERROR_LOG = "error_log";
    public static final String COL_ERROR_ID = "error_id";
    public static final String COL_ERR_SESSION_ID = "session_id";
    public static final String COL_ERROR_TYPE = "error_type";
    public static final String COL_ERROR_SUBTYPE = "error_subtype";
    public static final String COL_ORIGINAL_TEXT = "original_text";
    public static final String COL_SUGGESTION = "suggestion";
    public static final String COL_WAS_ACCEPTED = "was_accepted"; // 1 = fixed, 0 = dismissed

    // Table 3: chat_history
    public static final String TABLE_CHAT_HISTORY = "chat_history";
    public static final String COL_CHAT_ID = "chat_id";
    public static final String COL_USER_MESSAGE = "user_message";
    public static final String COL_BOT_RESPONSE = "bot_response";
    public static final String COL_CONTEXT_TEXT = "context_text";
    public static final String COL_CHAT_TIMESTAMP = "timestamp";

    // Table 4: test_results
    public static final String TABLE_TEST_RESULTS = "test_results";
    public static final String COL_TEST_ID = "test_id";
    public static final String COL_TEST_SCORE = "score";
    public static final String COL_TEST_ATTEMPT = "attempt"; // 1 or 2
    public static final String COL_TEST_TIMESTAMP = "timestamp";

    private static final String CREATE_SESSIONS_TABLE =
            "CREATE TABLE " + TABLE_SESSIONS + " (" +
            COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TEXT_CONTENT + " TEXT NOT NULL, " +
            COL_GRAMMAR_SCORE + " INTEGER, " +
            COL_CLARITY_SCORE + " INTEGER, " +
            COL_CONTEXT_MODE + " TEXT, " +
            COL_TONE_DETECTED + " TEXT, " +
            COL_WORD_COUNT + " INTEGER, " +
            COL_TIMESTAMP + " TEXT)";

    private static final String CREATE_ERROR_LOG_TABLE =
            "CREATE TABLE " + TABLE_ERROR_LOG + " (" +
            COL_ERROR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ERR_SESSION_ID + " INTEGER, " +
            COL_ERROR_TYPE + " TEXT, " +
            COL_ERROR_SUBTYPE + " TEXT, " +
            COL_ORIGINAL_TEXT + " TEXT, " +
            COL_SUGGESTION + " TEXT, " +
            COL_WAS_ACCEPTED + " INTEGER)";

    private static final String CREATE_CHAT_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_CHAT_HISTORY + " (" +
            COL_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USER_MESSAGE + " TEXT, " +
            COL_BOT_RESPONSE + " TEXT, " +
            COL_CONTEXT_TEXT + " TEXT, " +
            COL_CHAT_TIMESTAMP + " TEXT)";

    private static final String CREATE_TEST_RESULTS_TABLE =
            "CREATE TABLE " + TABLE_TEST_RESULTS + " (" +
            COL_TEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TEST_SCORE + " INTEGER, " +
            COL_TEST_ATTEMPT + " INTEGER, " +
            COL_TEST_TIMESTAMP + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SESSIONS_TABLE);
        db.execSQL(CREATE_ERROR_LOG_TABLE);
        db.execSQL(CREATE_CHAT_HISTORY_TABLE);
        db.execSQL(CREATE_TEST_RESULTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TEST_RESULTS_TABLE);
        }
    }
}
