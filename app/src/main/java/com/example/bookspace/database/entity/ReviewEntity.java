package com.example.bookspace.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "reviews", primaryKeys = {"userId", "bookId"})
public class ReviewEntity {
    @NonNull
    public String userId = "";

    public int bookId;
    public int rating;        // 1..5
    public String content;    // nhận xét (nullable)
    public long createdAt;    // System.currentTimeMillis()
}
