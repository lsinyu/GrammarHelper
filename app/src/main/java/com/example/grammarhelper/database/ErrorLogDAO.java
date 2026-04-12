package com.example.grammarhelper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.grammarhelper.model.GrammarError;
import java.util.ArrayList;
import java.util.List;

public class ErrorLogDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ErrorLogDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertErrorLog(GrammarError error) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ERR_SESSION_ID, error.sessionId);
        values.put(DatabaseHelper.COL_ERROR_TYPE, error.errorType);
        values.put(DatabaseHelper.COL_ERROR_SUBTYPE, error.errorSubtype);
        values.put(DatabaseHelper.COL_ORIGINAL_TEXT, error.originalText);
        values.put(DatabaseHelper.COL_SUGGESTION, error.suggestion);
        values.put(DatabaseHelper.COL_WAS_ACCEPTED, error.wasAccepted);
        return db.insert(DatabaseHelper.TABLE_ERROR_LOG, null, values);
    }

    public List<GrammarError> getErrorsBySession(int sessionId) {
        List<GrammarError> errors = new ArrayList<>();
        String selection = DatabaseHelper.COL_ERR_SESSION_ID + " = ?";
        String[] selectionArgs = { String.valueOf(sessionId) };
        Cursor cursor = db.query(DatabaseHelper.TABLE_ERROR_LOG, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                GrammarError error = new GrammarError();
                error.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ERROR_ID));
                error.sessionId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ERR_SESSION_ID));
                error.errorType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ERROR_TYPE));
                error.errorSubtype = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ERROR_SUBTYPE));
                error.originalText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORIGINAL_TEXT));
                error.suggestion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUGGESTION));
                error.wasAccepted = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WAS_ACCEPTED));
                errors.add(error);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return errors;
    }

    public Cursor getTopMistakes() {
        String query = "SELECT " + DatabaseHelper.COL_ERROR_SUBTYPE + ", COUNT(*) as count " +
                " FROM " + DatabaseHelper.TABLE_ERROR_LOG +
                " GROUP BY " + DatabaseHelper.COL_ERROR_SUBTYPE +
                " ORDER BY count DESC LIMIT 5";
        return db.rawQuery(query, null);
    }

    public Cursor getErrorDistribution() {
        String query = "SELECT " + DatabaseHelper.COL_ERROR_TYPE + ", COUNT(*) as count " +
                " FROM " + DatabaseHelper.TABLE_ERROR_LOG +
                " GROUP BY " + DatabaseHelper.COL_ERROR_TYPE;
        return db.rawQuery(query, null);
    }

    public void clearErrorLog() {
        db.delete(DatabaseHelper.TABLE_ERROR_LOG, null, null);
    }

    public int getTotalFixedCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ERROR_LOG + 
            " WHERE " + DatabaseHelper.COL_WAS_ACCEPTED + " = 1", null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }
}
