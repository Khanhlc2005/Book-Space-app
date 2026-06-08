package com.example.bookspace.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.ChallengeDao;
import com.example.bookspace.database.entity.ChallengeEntity;

import java.util.List;

public class ChallengeRepository {
    private final ChallengeDao challengeDao;

    public ChallengeRepository(Context context) {
        challengeDao = AppDatabase.getInstance(context).challengeDao();
    }

    // ── Đọc (LiveData cho UI observe) ──────────────────────────────

    public LiveData<ChallengeEntity> getActiveChallenge() {
        return challengeDao.getActiveChallenge();
    }

    public LiveData<List<ChallengeEntity>> getAllChallenges() {
        return challengeDao.getAllChallenges();
    }

    public LiveData<ChallengeEntity> getActivePageChallengeForBook(int bookId) {
        return challengeDao.getActivePageChallengeForBook(bookId);
    }

    // ── Ghi (chạy trên background thread) ──────────────────────────

    /** Tạo thử thách mới. */
    public void insert(ChallengeEntity challenge) {
        AppDatabase.databaseWriteExecutor.execute(() -> challengeDao.insert(challenge));
    }

    /** Cập nhật toàn bộ entity (VD: sửa tên, đổi mục tiêu). */
    public void update(ChallengeEntity challenge) {
        AppDatabase.databaseWriteExecutor.execute(() -> challengeDao.update(challenge));
    }

    /** Cộng thêm tiến độ cho thử thách. */
    public void updateProgress(int challengeId, int increment) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                challengeDao.updateChallengeProgress(challengeId, increment));
    }

    /** Đánh dấu hoàn thành. */
    public void markAsCompleted(int challengeId) {
        AppDatabase.databaseWriteExecutor.execute(() -> challengeDao.markAsCompleted(challengeId));
    }

    /** Đánh dấu thất bại. */
    public void markAsFailed(int challengeId) {
        AppDatabase.databaseWriteExecutor.execute(() -> challengeDao.markAsFailed(challengeId));
    }

    /** Phiên bản đồng bộ – chỉ gọi từ background thread. */
    public ChallengeEntity getActiveChallengeSync() {
        return challengeDao.getActiveChallengeSync();
    }
}
