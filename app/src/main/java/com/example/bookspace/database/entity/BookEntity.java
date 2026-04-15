package com.example.bookspace.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class BookEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String title;
    public String author;
    public String coverUrl;
    public int pages;
    public String description;
    public String category;
}
