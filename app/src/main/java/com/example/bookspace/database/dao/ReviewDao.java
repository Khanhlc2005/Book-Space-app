package com.example.bookspace.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bookspace.database.entity.ReviewEntity;

import java.util.List;

@Dao
public interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertReview(ReviewEntity review);

    @Query("SELECT * FROM reviews WHERE bookId = :bookId ORDER BY createdAt DESC")
    List<ReviewEntity> getReviewsForBook(int bookId);

    @Query("SELECT * FROM reviews WHERE userId = :userId AND bookId = :bookId")
    ReviewEntity getUserReview(String userId, int bookId);

    @Query("SELECT AVG(rating) FROM reviews WHERE bookId = :bookId")
    Double getAverageRating(int bookId);

    @Query("SELECT COUNT(*) FROM reviews WHERE bookId = :bookId")
    int getReviewCount(int bookId);

    @Query("DELETE FROM reviews WHERE userId = :userId AND bookId = :bookId")
    void deleteReview(String userId, int bookId);
}
