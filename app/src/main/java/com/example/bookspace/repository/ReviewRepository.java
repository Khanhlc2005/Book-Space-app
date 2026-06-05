package com.example.bookspace.repository;

import android.content.Context;

import com.example.bookspace.SessionManager;
import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.ReviewDao;
import com.example.bookspace.database.entity.ReviewEntity;

import java.util.List;

public class ReviewRepository {
    private final ReviewDao reviewDao;
    private final String userId;

    public ReviewRepository(Context context) {
        reviewDao = AppDatabase.getInstance(context).reviewDao();
        userId = SessionManager.getCurrentUserId(context);
    }

    public List<ReviewEntity> getReviews(int bookId) {
        return reviewDao.getReviewsForBook(bookId);
    }

    public ReviewEntity getMyReview(int bookId) {
        return reviewDao.getUserReview(userId, bookId);
    }

    /** Gửi/sửa review của người dùng hiện tại (composite key (userId, bookId) + REPLACE). */
    public void submitReview(int bookId, int rating, String content) {
        ReviewEntity review = new ReviewEntity();
        review.userId = userId;
        review.bookId = bookId;
        review.rating = rating;
        review.content = content;
        review.createdAt = System.currentTimeMillis();
        reviewDao.upsertReview(review);
    }

    public void deleteMyReview(int bookId) {
        reviewDao.deleteReview(userId, bookId);
    }

    public double getAverage(int bookId) {
        Double avg = reviewDao.getAverageRating(bookId);
        return avg == null ? 0d : avg;
    }

    public int getCount(int bookId) {
        return reviewDao.getReviewCount(bookId);
    }

    public String getCurrentUserId() {
        return userId;
    }
}
