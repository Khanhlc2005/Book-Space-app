package com.example.bookspace.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "reading_progress", primaryKeys = {"userId", "bookId"})
public class ReadingProgressEntity {
    @NonNull 
    public String userId = "";
    
    public int bookId;
    public int currentPage;
    public int totalPages;
    public long lastReadAt;
}
