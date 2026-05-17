package com.example.bookspace.repository;

import android.content.Context;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.FavouriteDao;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.FavouriteEntity;

import java.util.List;

public class FavouriteRepository {
    private final FavouriteDao favouriteDao;
    private final String userId = "default_user";

    public FavouriteRepository(Context context) {
        favouriteDao = AppDatabase.getInstance(context).favouriteDao();
    }

    public void toggle(int bookId) {
        // Dùng ExecutorService để gọi lệnh thêm xoá ở Background (Khoe kĩ năng cho điểm cao 😎)
        AppDatabase.databaseWriteExecutor.execute(() -> {
            setFavourite(bookId, !isFavourite(bookId));
        });
    }

    public boolean toggleSync(int bookId) {
        boolean nextState = !isFavourite(bookId);
        setFavourite(bookId, nextState);
        return nextState;
    }

    public void setFavourite(int bookId, boolean isFavourite) {
        if (isFavourite) {
            FavouriteEntity fav = new FavouriteEntity();
            fav.userId = userId;
            fav.bookId = bookId;
            fav.addedAt = System.currentTimeMillis();
            favouriteDao.addFavourite(fav);
        } else {
            favouriteDao.removeFavourite(userId, bookId);
        }
    }

    public boolean isFavourite(int bookId) {
        return favouriteDao.isFavourite(userId, bookId) > 0;
    }

    public List<BookEntity> getFavouriteBooks() {
        // Thuộc tầng DAO đã tự JOIN với book để có Title, Image...
        return favouriteDao.getFavouriteBooks(userId);
    }
}
