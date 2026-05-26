package com.example.bookspace.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "books",
        indices = {
                @Index(value = {"title", "author"}, unique = true)
        }
)
public class BookEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String title;
    public String author;
    public String coverUrl;
    public int pages;
    public String description;
    public String category;
    public boolean isDownloaded;
    public String bookFilePath;  // Đường dẫn file sách trong assets, VD: "books/dac_nhan_tam.txt"
}
