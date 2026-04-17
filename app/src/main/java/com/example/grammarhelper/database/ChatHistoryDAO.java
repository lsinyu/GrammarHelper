package com.example.grammarhelper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.grammarhelper.model.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ChatHistoryDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertChatMessage(ChatMessage message) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_MESSAGE, message.userMessage);
        values.put(DatabaseHelper.COL_BOT_RESPONSE, message.botResponse);
        values.put(DatabaseHelper.COL_CONTEXT_TEXT, message.contextText);
        values.put(DatabaseHelper.COL_CHAT_TIMESTAMP, message.timestamp);
        return db.insert(DatabaseHelper.TABLE_CHAT_HISTORY, null, values);
    }

    public List<ChatMessage> getAllChatHistory() {
        List<ChatMessage> chatMessages = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CHAT_HISTORY, null, null, null, null, null, DatabaseHelper.COL_CHAT_TIMESTAMP + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage();
                message.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CHAT_ID));
                message.userMessage = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_MESSAGE));
                message.botResponse = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOT_RESPONSE));
                message.contextText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTEXT_TEXT));
                message.timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CHAT_TIMESTAMP));
                chatMessages.add(message);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return chatMessages;
    }

    public void clearHistory() {
        db.delete(DatabaseHelper.TABLE_CHAT_HISTORY, null, null);
    }
}
