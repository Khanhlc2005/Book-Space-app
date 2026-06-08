package com.example.bookspace.database.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Giao dịch mượn sách.
 * Liên kết trực tiếp với {@link BookEntity} thông qua Foreign Key.
 * Khi cuốn sách bị xóa khỏi bảng books, các bản ghi mượn liên quan cũng bị xóa theo (CASCADE).
 */
@Entity(
    tableName = "book_loans",
    foreignKeys = @ForeignKey(
        entity = BookEntity.class,
        parentColumns = "id",
        childColumns = "bookId",
        onDelete = CASCADE
    ),
    indices = @Index(value = "bookId")
)
public class BookLoanEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int bookId;              // FK → books.id

    public long borrowDate;         // Ngày mượn – System.currentTimeMillis()
    public long dueDate;            // Hạn trả
    public long returnDate;         // Ngày trả thực tế (0 nếu chưa trả)

    /**
     * Trạng thái giao dịch: "borrowed", "returned", "overdue".
     */
    public String status;
}
