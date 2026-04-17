package com.example.grammarhelper.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.grammarhelper.R;
import com.example.grammarhelper.ui.SmartEditorActivity;
import java.util.Calendar;
import java.util.Random;

public class NotificationHelper {

    private static final String CHANNEL_ID = "grammar_tips_channel";
    private static final int NOTIFICATION_ID = 1001;

    private static final String[] GRAMMAR_TIPS = {
        "💡 Tip: \"Their\" is possessive, \"there\" refers to a place, and \"they're\" means \"they are\".",
        "💡 Tip: Use \"fewer\" for countable items and \"less\" for uncountable quantities.",
        "💡 Tip: \"Affect\" is usually a verb, \"effect\" is usually a noun.",
        "💡 Tip: Avoid passive voice when possible — \"The dog bit the man\" is stronger than \"The man was bitten.\"",
        "💡 Tip: Use a comma before coordinating conjunctions (for, and, nor, but, or, yet, so) in compound sentences.",
        "💡 Tip: \"Who\" is for subjects, \"whom\" is for objects. Try substituting \"he/him\" to check.",
        "💡 Tip: \"Its\" is possessive, \"It's\" is a contraction of \"it is.\"",
        "💡 Tip: Avoid \"very\" — use stronger words: \"very tired\" → \"exhausted\", \"very happy\" → \"thrilled\".",
        "💡 Tip: Use parallel structure in lists: \"She likes reading, writing, and painting\" not \"to read, writing, and paint.\"",
        "💡 Tip: End sentences with strong words for more impact. Move weak endings to the middle."
    };

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Daily Grammar Tips",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Receive daily grammar tips to improve your writing");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void scheduleDailyTip(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, GrammarTipReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If time has passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            );
        }
    }

    public static void cancelDailyTip(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, GrammarTipReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void showTipNotification(Context context) {
        createNotificationChannel(context);

        String tip = GRAMMAR_TIPS[new Random().nextInt(GRAMMAR_TIPS.length)];

        Intent intent = new Intent(context, SmartEditorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Daily Grammar Tip")
            .setContentText(tip)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(tip))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public static class GrammarTipReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showTipNotification(context);
        }
    }
}
