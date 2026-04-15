package com.example.bookspace.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "favourites", primaryKeys = {"userId", "bookId"})
public class FavouriteEntity {
    @NonNull 
    public String userId = "";
    
    public int bookId;
    public long addedAt;
}
