package com.example.bookspace.repository;

import android.content.Context;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.BookDao;
import com.example.bookspace.database.entity.BookEntity;

import java.util.List;

public class BookRepository {
    private final BookDao bookDao;

    public BookRepository(Context context) {
        bookDao = AppDatabase.getInstance(context).bookDao();
    }

    public List<BookEntity> getAllBooks() {
        return bookDao.getAllBooks();
    }

    public List<BookEntity> getByCategory(String category) {
        return bookDao.getBooksByCategory(category);
    }

    public List<BookEntity> searchBooks(String keyword) {
        return bookDao.searchBooks(keyword);
    }
}
