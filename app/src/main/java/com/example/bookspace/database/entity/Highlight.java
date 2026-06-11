package com.example.bookspace.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "highlights")
public class Highlight {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int bookId;
    public String highlightedText;
    public String chapterName;
    public int chapterIndex;
    public int characterOffsetStart;

    public Highlight() {}

    public Highlight(int bookId, String highlightedText, String chapterName, int chapterIndex, int characterOffsetStart) {
        this.bookId = bookId;
        this.highlightedText = highlightedText;
        this.chapterName = chapterName;
        this.chapterIndex = chapterIndex;
        this.characterOffsetStart = characterOffsetStart;
    }
}
