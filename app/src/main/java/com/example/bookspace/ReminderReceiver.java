package com.example.bookspace;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "reading_reminder_channel";
    public static final String ACTION_SNOOZE = "com.example.bookspace.ACTION_SNOOZE";
    public static final String ACTION_SHOW_REMINDER = "com.example.bookspace.ACTION_SHOW_REMINDER";
    public static final String EXTRA_REMINDER_ID = "reminder_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);
        String action = intent.getAction();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (ACTION_SNOOZE.equals(action)) {
            if (reminderId != -1) {
                snoozeAlarm(context, reminderId);
                notificationManager.cancel(reminderId);
            }
            return;
        }

        // Tạo Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc nhở đọc sách",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, CurrentlyReadingListActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context, reminderId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra(EXTRA_REMINDER_ID, reminderId);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context, reminderId, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_clock)
                .setContentTitle("📚 Đến giờ đọc sách!")
                .setContentText("Dành ít phút để tiếp tục hành trình khám phá tri thức nhé.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .addAction(R.drawable.ic_menu_book, "ĐỌC NGAY", openPendingIntent)
                .addAction(R.drawable.ic_clock, "NHẮC LẠI (15P)", snoozePendingIntent)
                .setContentIntent(openPendingIntent);

        notificationManager.notify(reminderId, builder.build());
    }

    private void snoozeAlarm(Context context, int id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_SHOW_REMINDER);
        intent.putExtra(EXTRA_REMINDER_ID, id);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}
