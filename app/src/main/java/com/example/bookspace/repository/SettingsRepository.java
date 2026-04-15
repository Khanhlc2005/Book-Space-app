package com.example.bookspace.repository;

import android.content.Context;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.ReadingSettingsDao;
import com.example.bookspace.database.entity.ReadingSettingsEntity;

public class SettingsRepository {
    private final ReadingSettingsDao settingsDao;
    private final String userId = "user1";

    public SettingsRepository(Context context) {
        settingsDao = AppDatabase.getInstance(context).readingSettingsDao();
    }

    public ReadingSettingsEntity getSettings() {
        ReadingSettingsEntity s = settingsDao.getSettings(userId);
        if (s == null) {
            s = new ReadingSettingsEntity();
            s.userId = userId;
            s.fontSize = 19;
            s.fontFamily = "literata";
            s.theme = "light";
            s.brightness = 70;
        }
        return s;
    }

    public void saveSettings(int fontSize, String fontFamily, String theme, int brightness) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ReadingSettingsEntity s = new ReadingSettingsEntity();
            s.userId = userId;
            s.fontSize = fontSize;
            s.fontFamily = fontFamily;
            s.theme = theme;
            s.brightness = brightness;
            settingsDao.saveSettings(s);
        });
    }
}
