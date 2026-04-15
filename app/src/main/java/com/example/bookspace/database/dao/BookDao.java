package com.example.bookspace.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bookspace.database.entity.BookEntity;

import java.util.List;

@Dao
public interface BookDao {
    @Query("SELECT * FROM books")
    List<BookEntity> getAllBooks();

    @Query("SELECT * FROM books WHERE category = :category")
    List<BookEntity> getBooksByCategory(String category);

    @Query("SELECT * FROM books WHERE title LIKE '%' || :keyword || '%'")
    List<BookEntity> searchBooks(String keyword);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookEntity> books);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookEntity book);
}
