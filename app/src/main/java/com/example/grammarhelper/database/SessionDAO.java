package com.example.grammarhelper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.grammarhelper.model.Session;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public SessionDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertSession(Session session) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TEXT_CONTENT, session.textContent);
        values.put(DatabaseHelper.COL_GRAMMAR_SCORE, session.grammarScore);
        values.put(DatabaseHelper.COL_CLARITY_SCORE, session.clarityScore);
        values.put(DatabaseHelper.COL_CONTEXT_MODE, session.contextMode);
        values.put(DatabaseHelper.COL_TONE_DETECTED, session.toneDetected);
        values.put(DatabaseHelper.COL_WORD_COUNT, session.wordCount);
        values.put(DatabaseHelper.COL_TIMESTAMP, session.timestamp);
        return db.insert(DatabaseHelper.TABLE_SESSIONS, null, values);
    }

    public List<Session> getAllSessions() {
        List<Session> sessions = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SESSIONS, null, null, null, null, null, DatabaseHelper.COL_TIMESTAMP + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Session session = new Session();
                session.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_ID));
                session.textContent = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEXT_CONTENT));
                session.grammarScore = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GRAMMAR_SCORE));
                session.clarityScore = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CLARITY_SCORE));
                session.contextMode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTEXT_MODE));
                session.toneDetected = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TONE_DETECTED));
                session.wordCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORD_COUNT));
                session.timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TIMESTAMP));
                sessions.add(session);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return sessions;
    }

    public Cursor getScoreTrend() {
        String query = "SELECT " + DatabaseHelper.COL_GRAMMAR_SCORE + ", " + DatabaseHelper.COL_TIMESTAMP +
                " FROM " + DatabaseHelper.TABLE_SESSIONS +
                " ORDER BY " + DatabaseHelper.COL_TIMESTAMP + " ASC LIMIT 30";
        return db.rawQuery(query, null);
    }

    public int getSessionCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SESSIONS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getStreakDays() {
        // Count consecutive days with sessions, working backwards from today
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT date(" + DatabaseHelper.COL_TIMESTAMP + ") as day FROM " +
                        DatabaseHelper.TABLE_SESSIONS + " ORDER BY day DESC", null);

        if (cursor == null || !cursor.moveToFirst()) return 0;

        int streak = 0;
        java.util.Calendar expected = java.util.Calendar.getInstance();
        expected.set(java.util.Calendar.HOUR_OF_DAY, 0);
        expected.set(java.util.Calendar.MINUTE, 0);
        expected.set(java.util.Calendar.SECOND, 0);
        expected.set(java.util.Calendar.MILLISECOND, 0);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        do {
            try {
                String dayStr = cursor.getString(0);
                java.util.Date day = sdf.parse(dayStr);
                if (day == null) break;

                java.util.Calendar dayCal = java.util.Calendar.getInstance();
                dayCal.setTime(day);
                dayCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                dayCal.set(java.util.Calendar.MINUTE, 0);
                dayCal.set(java.util.Calendar.SECOND, 0);
                dayCal.set(java.util.Calendar.MILLISECOND, 0);

                if (dayCal.equals(expected)) {
                    // Found a match for expected day
                    streak++;
                    expected.add(java.util.Calendar.DAY_OF_YEAR, -1);
                } else if (streak == 0) {
                    // First iteration: check if their most recent session was yesterday
                    java.util.Calendar yesterday = (java.util.Calendar) expected.clone();
                    yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1);

                    if (dayCal.equals(yesterday)) {
                        streak++;
                        expected = yesterday; // Sync expected to yesterday
                        expected.add(java.util.Calendar.DAY_OF_YEAR, -1);
                    } else {
                        break; // Latest session was more than 1 day ago
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        } while (cursor.moveToNext());

        cursor.close();
        return streak;
    }
}
