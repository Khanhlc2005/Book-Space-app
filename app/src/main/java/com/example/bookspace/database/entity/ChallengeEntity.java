package com.example.bookspace.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Thử thách đọc sách.
 *
 * Hỗ trợ 2 loại mục tiêu thông qua trường {@link #challengeType}:
 * <ul>
 *   <li>{@code "pages"} – Mục tiêu theo số trang (VD: hôm nay đọc 50 trang cuốn X).
 *       Khi đó {@link #bookId} trỏ đến cuốn sách cụ thể,
 *       {@link #targetValue} = số trang cần đọc,
 *       {@link #currentValue} = số trang đã đọc.</li>
 *   <li>{@code "books"} – Mục tiêu theo số quyển (VD: tháng này đọc 5 quyển).
 *       Khi đó {@link #bookId} = 0 (không gắn sách cụ thể),
 *       {@link #targetValue} = số quyển cần đọc,
 *       {@link #currentValue} = số quyển đã hoàn thành.</li>
 * </ul>
 */
@Entity(tableName = "challenges")
public class ChallengeEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;            // Tên thử thách, VD: "Đọc 50 trang Đắc Nhân Tâm"

    /**
     * Loại thử thách: "pages" (theo trang) hoặc "books" (theo quyển).
     */
    public String challengeType;    // "pages" | "books"

    /**
     * ID cuốn sách gắn với thử thách (chỉ dùng khi challengeType = "pages").
     * Bằng 0 nếu thử thách tính theo số quyển.
     */
    public int bookId;

    public int targetValue;         // Mục tiêu cần đạt (số trang hoặc số quyển)
    public int currentValue;        // Tiến độ hiện tại

    public long startDate;          // Ngày bắt đầu – System.currentTimeMillis()
    public long endDate;            // Ngày kết thúc

    /**
     * Trạng thái thử thách: "active", "completed", "failed".
     */
    public String status;
}
