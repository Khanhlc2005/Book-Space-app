package com.example.bookspace;

import android.content.Context;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.repository.FavouriteRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý danh sách yêu thích, kết nối giữa Repository và UI.
 */
public class FavouriteManager {
    private final FavouriteRepository repository;

    public FavouriteManager(Context context) {
        repository = new FavouriteRepository(context);
    }

    /**
     * Truy vấn và quản lý danh sách yêu thích, trả về List<Book>
     */
    public List<Book> getFauvourites() {
        List<BookEntity> entities = repository.getFavouriteBooks();
        List<Book> books = new ArrayList<>();
        for (BookEntity entity : entities) {
            books.add(Book.fromEntity(entity));
        }
        return books;
    }

    public void toggleFavourite(int bookId) {
        repository.toggle(bookId);
    }

    public boolean isFavourite(int bookId) {
        return repository.isFavourite(bookId);
    }
}
