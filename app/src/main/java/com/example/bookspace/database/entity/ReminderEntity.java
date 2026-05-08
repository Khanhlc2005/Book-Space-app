package com.example.bookspace.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class ReminderEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int hour;
    public int minute;
    public boolean isActive;
    
    public ReminderEntity(int hour, int minute, boolean isActive) {
        this.hour = hour;
        this.minute = minute;
        this.isActive = isActive;
    }
}
