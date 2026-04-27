package com.example.bookspace;

import java.io.Serializable;

public class Reminder implements Serializable {
    private int id;
    private int hour;
    private int minute;
    private boolean isEnabled;

    public Reminder(int id, int hour, int minute, boolean isEnabled) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.isEnabled = isEnabled;
    }

    public int getId() {
        return id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }
}