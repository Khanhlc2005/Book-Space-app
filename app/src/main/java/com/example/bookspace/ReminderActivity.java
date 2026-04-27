package com.example.bookspace;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookspace.databinding.ActivityReminderBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderActivity extends AppCompatActivity implements ReminderAdapter.OnReminderChangeListener {

    private ActivityReminderBinding binding;
    private List<Reminder> reminderList;
    private ReminderAdapter adapter;
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainReminder, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBarReminder.setPadding(
                    binding.topBarReminder.getPaddingLeft(),
                    insets.top + 16,
                    binding.topBarReminder.getPaddingRight(),
                    binding.topBarReminder.getPaddingBottom()
            );
            return WindowInsetsCompat.CONSUMED;
        });

        sharedPreferences = getSharedPreferences("BookSpaceReminders", MODE_PRIVATE);
        loadReminders();

        binding.recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList, this);
        binding.recyclerReminders.setAdapter(adapter);

        binding.btnBackReminder.setOnClickListener(v -> finish());
        binding.btnAddReminder.setOnClickListener(v -> showTimePickerDialog());

        updateEmptyView();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            int id = (int) System.currentTimeMillis();
            Reminder reminder = new Reminder(id, hourOfDay, minuteOfHour, true);
            reminderList.add(reminder);
            saveReminders();
            adapter.notifyItemInserted(reminderList.size() - 1);
            setAlarm(reminder);
            updateEmptyView();
        }, hour, minute, true).show();
    }

    private void setAlarm(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("REMINDER_ID", reminder.getId());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, reminder.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void cancelAlarm(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, reminder.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void onToggle(Reminder reminder, boolean isEnabled) {
        if (isEnabled) {
            setAlarm(reminder);
        } else {
            cancelAlarm(reminder);
        }
        saveReminders();
    }

    @Override
    public void onDelete(Reminder reminder) {
        cancelAlarm(reminder);
        reminderList.remove(reminder);
        saveReminders();
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void loadReminders() {
        String json = sharedPreferences.getString("reminders", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Reminder>>() {}.getType();
            reminderList = gson.fromJson(json, type);
        } else {
            reminderList = new ArrayList<>();
        }
    }

    private void saveReminders() {
        String json = gson.toJson(reminderList);
        sharedPreferences.edit().putString("reminders", json).apply();
    }

    private void updateEmptyView() {
        binding.tvEmptyReminders.setVisibility(reminderList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}