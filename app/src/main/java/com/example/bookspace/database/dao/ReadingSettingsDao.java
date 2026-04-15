package com.example.bookspace.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bookspace.database.entity.ReadingSettingsEntity;

@Dao
public interface ReadingSettingsDao {
    @Query("SELECT * FROM reading_settings WHERE userId = :userId")
    ReadingSettingsEntity getSettings(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSettings(ReadingSettingsEntity settings);
}
