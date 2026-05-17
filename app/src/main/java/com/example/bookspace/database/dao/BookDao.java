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

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    BookEntity getBookById(int bookId);

    @Query("SELECT * FROM books WHERE title = :title AND author = :author LIMIT 1")
    BookEntity findByTitleAndAuthor(String title, String author);
    @Query("SELECT * FROM books WHERE category = :category")
    List<BookEntity> getBooksByCategory(String category);

    @Query("SELECT * FROM books WHERE title LIKE '%' || :keyword || '%' OR author LIKE '%' || :keyword || '%'")
    List<BookEntity> searchBooks(String keyword);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookEntity> books);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BookEntity book);

    @Query("UPDATE books SET coverUrl = :coverUrl, pages = :pages, description = :description, category = :category WHERE id = :bookId")
    void updateBookDetails(int bookId, String coverUrl, int pages, String description, String category);

    @Query("SELECT isDownloaded FROM books WHERE id = :bookId LIMIT 1")
    boolean isDownloaded(int bookId);

    @Query("UPDATE books SET isDownloaded = :isDownloaded WHERE id = :bookId")
    void updateDownloadedState(int bookId, boolean isDownloaded);
}
