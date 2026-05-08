package com.example.bookspace;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.entity.ReminderEntity;
import java.util.Calendar;
import java.util.List;

public class ReminderActivity extends AppCompatActivity implements ReminderAdapter.OnReminderClickListener {

    private RecyclerView rvReminders;
    private ReminderAdapter adapter;
    private AppDatabase db;
    private boolean isEditMode = false;
    private TextView btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        db = AppDatabase.getInstance(this);
        
        rvReminders = findViewById(R.id.rvReminders);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter();
        adapter.setOnReminderClickListener(this);
        rvReminders.setAdapter(adapter);

        // Nút quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnAdd).setOnClickListener(v -> showTimePickerDialog(null));
        
        btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            btnEdit.setText(isEditMode ? "Xong" : "Xoá");
            adapter.setEditMode(isEditMode);
        });

        loadReminders();
    }

    private void loadReminders() {
        List<ReminderEntity> reminders = db.reminderDao().getAllReminders();
        adapter.setReminders(reminders);
    }

    private void showTimePickerDialog(ReminderEntity reminder) {
        Calendar c = Calendar.getInstance();
        int hour = (reminder != null) ? reminder.hour : c.get(Calendar.HOUR_OF_DAY);
        int minute = (reminder != null) ? reminder.minute : c.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            if (reminder == null) {
                // Thêm mới
                ReminderEntity newReminder = new ReminderEntity(hourOfDay, minuteOfHour, true);
                long id = db.reminderDao().insert(newReminder);
                newReminder.id = (int) id;
                scheduleAlarm(newReminder);
            } else {
                // Cập nhật
                cancelAlarm(reminder);
                reminder.hour = hourOfDay;
                reminder.minute = minuteOfHour;
                reminder.isActive = true;
                db.reminderDao().update(reminder);
                scheduleAlarm(reminder);
            }
            loadReminders();
        }, hour, minute, true);
        timePicker.show();
    }

    @Override
    public void onReminderClick(ReminderEntity reminder) {
        showTimePickerDialog(reminder);
    }

    @Override
    public void onReminderToggle(ReminderEntity reminder, boolean isActive) {
        reminder.isActive = isActive;
        db.reminderDao().update(reminder);
        if (isActive) {
            scheduleAlarm(reminder);
        } else {
            cancelAlarm(reminder);
        }
    }

    @Override
    public void onReminderDelete(ReminderEntity reminder) {
        cancelAlarm(reminder);
        db.reminderDao().delete(reminder);
        loadReminders();
        Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
    }

    private void scheduleAlarm(ReminderEntity reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_SHOW_REMINDER);
        // Dùng ID làm requestCode để phân biệt các báo thức khác nhau
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, reminder.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, reminder.hour);
        calendar.set(Calendar.MINUTE, reminder.minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void cancelAlarm(ReminderEntity reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_SHOW_REMINDER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, reminder.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
