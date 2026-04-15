package com.example.bookspace.repository;

import android.content.Context;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.ReadingProgressDao;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.ReadingProgressEntity;

import java.util.List;

public class ProgressRepository {
    private final ReadingProgressDao progressDao;
    private final String userId = "user1";

    public ProgressRepository(Context context) {
        progressDao = AppDatabase.getInstance(context).readingProgressDao();
    }

    public void updateProgress(int bookId, int currentPage, int totalPages) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ReadingProgressEntity p = new ReadingProgressEntity();
            p.userId = userId;
            p.bookId = bookId;
            p.currentPage = currentPage;
            p.totalPages = totalPages;
            p.lastReadAt = System.currentTimeMillis();
            progressDao.updateProgress(p);
        });
    }

    public ReadingProgressEntity getProgress(int bookId) {
        return progressDao.getProgress(userId, bookId);
    }

    public List<BookEntity> getBooksInReadingProgress() {
        return progressDao.getBooksInReadingProgress(userId);
    }
}
