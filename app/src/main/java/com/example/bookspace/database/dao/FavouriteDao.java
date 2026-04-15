package com.example.bookspace.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.FavouriteEntity;

import java.util.List;

@Dao
public interface FavouriteDao {
    @Query("SELECT books.* FROM books INNER JOIN favourites ON books.id = favourites.bookId WHERE favourites.userId = :userId ORDER BY favourites.addedAt DESC")
    List<BookEntity> getFavouriteBooks(String userId);

    @Query("SELECT COUNT(*) FROM favourites WHERE userId = :userId AND bookId = :bookId")
    int isFavourite(String userId, int bookId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFavourite(FavouriteEntity favourite);

    @Query("DELETE FROM favourites WHERE userId = :userId AND bookId = :bookId")
    void removeFavourite(String userId, int bookId);
}
