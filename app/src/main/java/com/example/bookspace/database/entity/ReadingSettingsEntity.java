package com.example.bookspace.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reading_settings")
public class ReadingSettingsEntity {
    @PrimaryKey
    @NonNull
    public String userId = "";
    
    public int fontSize;
    public String fontFamily;
    public String theme;
    public int brightness;
}
