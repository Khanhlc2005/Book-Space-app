package com.example.bookspace.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.bookspace.database.entity.ChallengeEntity;

import java.util.List;

@Dao
public interface ChallengeDao {

    @Insert
    void insert(ChallengeEntity challenge);

    @Update
    void update(ChallengeEntity challenge);

    /** Lấy thử thách đang hoạt động (nếu có). */
    @Query("SELECT * FROM challenges WHERE status = 'active' ORDER BY startDate DESC LIMIT 1")
    LiveData<ChallengeEntity> getActiveChallenge();

    /** Phiên bản đồng bộ – dùng trong Repository khi cần kiểm tra nhanh trên background thread. */
    @Query("SELECT * FROM challenges WHERE status = 'active' ORDER BY startDate DESC LIMIT 1")
    ChallengeEntity getActiveChallengeSync();

    /** Cộng thêm tiến độ cho thử thách. */
    @Query("UPDATE challenges SET currentValue = currentValue + :increment WHERE id = :challengeId")
    void updateChallengeProgress(int challengeId, int increment);

    /** Đánh dấu thử thách đã hoàn thành. */
    @Query("UPDATE challenges SET status = 'completed' WHERE id = :challengeId")
    void markAsCompleted(int challengeId);

    /** Đánh dấu thử thách thất bại (quá hạn). */
    @Query("UPDATE challenges SET status = 'failed' WHERE id = :challengeId")
    void markAsFailed(int challengeId);

    /** Lấy lịch sử tất cả thử thách. */
    @Query("SELECT * FROM challenges ORDER BY startDate DESC")
    LiveData<List<ChallengeEntity>> getAllChallenges();

    /** Lấy các thử thách theo loại (pages/books) đang active cho 1 cuốn sách cụ thể. */
    @Query("SELECT * FROM challenges WHERE bookId = :bookId AND challengeType = 'pages' AND status = 'active'")
    LiveData<ChallengeEntity> getActivePageChallengeForBook(int bookId);
}
