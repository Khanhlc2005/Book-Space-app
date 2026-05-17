package com.example.bookspace.repository;

import android.content.Context;

import com.example.bookspace.Book;
import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.BookDao;
import com.example.bookspace.database.entity.BookEntity;

import java.util.ArrayList;
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

    public List<Book> getBooksByAuthorExcept(String author, int bookId, int limit) {
        return toBooks(bookDao.getBooksByAuthorExcept(author, bookId, limit));
    }

    public List<BookEntity> searchBooks(String keyword) {
        return bookDao.searchBooks(keyword);
    }

    public int saveOrGetBookId(Book book) {
        if (book.getId() > 0) {
            return book.getId();
        }

        BookEntity existing = bookDao.findByTitleAndAuthor(book.getTitle(), book.getAuthor());
        if (existing != null) {
            book.setId(existing.id);
            bookDao.updateBookDetails(
                    existing.id,
                    book.getCoverUrl(),
                    book.getPages(),
                    book.getDescription(),
                    book.getCategory()
            );
            return existing.id;
        }

        BookEntity entity = toEntity(book);
        int bookId = (int) bookDao.insert(entity);
        book.setId(bookId);
        return bookId;
    }

    public boolean isDownloaded(int bookId) {
        return bookDao.isDownloaded(bookId);
    }

    public void markDownloaded(int bookId) {
        bookDao.updateDownloadedState(bookId, true);
    }

    public void removeDownloaded(int bookId) {
        bookDao.updateDownloadedState(bookId, false);
    private List<Book> toBooks(List<BookEntity> entities) {
        List<Book> books = new ArrayList<>();
        for (BookEntity entity : entities) {
            books.add(Book.fromEntity(entity));
        }
        return books;
    }

    private BookEntity toEntity(Book book) {
        BookEntity entity = new BookEntity();
        entity.title = book.getTitle();
        entity.author = book.getAuthor();
        entity.coverUrl = book.getCoverUrl();
        entity.pages = book.getPages();
        entity.description = book.getDescription();
        entity.category = book.getCategory();
        entity.isDownloaded = false;
        return entity;
    }

    public BookEntity getBookById(int bookId) {
        return bookDao.getBookById(bookId);
    }
}
