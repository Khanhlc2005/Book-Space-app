package com.example.bookspace.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.ReadingProgressEntity;

import java.util.List;

@Dao
public interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE userId = :userId AND bookId = :bookId")
    ReadingProgressEntity getProgress(String userId, int bookId);

    @Query("SELECT books.* FROM books INNER JOIN reading_progress ON books.id = reading_progress.bookId WHERE reading_progress.userId = :userId ORDER BY reading_progress.lastReadAt DESC")
    List<BookEntity> getBooksInReadingProgress(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateProgress(ReadingProgressEntity progress);
}
