package com.example.bookspace.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bookspace.database.entity.BookLoanEntity;

import java.util.List;

@Dao
public interface BookLoanDao {

    @Insert
    void insertLoan(BookLoanEntity loan);

    /** Tất cả giao dịch mượn, mới nhất lên đầu. */
    @Query("SELECT * FROM book_loans ORDER BY borrowDate DESC")
    LiveData<List<BookLoanEntity>> getAllLoans();

    /** Các giao dịch đang mượn (chưa trả). */
    @Query("SELECT * FROM book_loans WHERE status = 'borrowed' ORDER BY dueDate ASC")
    LiveData<List<BookLoanEntity>> getActiveLoans();

    /** Các giao dịch quá hạn. */
    @Query("SELECT * FROM book_loans WHERE status = 'borrowed' AND dueDate < :currentDate")
    LiveData<List<BookLoanEntity>> getOverdueLoans(long currentDate);

    /** Đánh dấu đã trả sách. */
    @Query("UPDATE book_loans SET status = 'returned', returnDate = :returnDate WHERE id = :loanId")
    void markAsReturned(int loanId, long returnDate);

    /** Đánh dấu quá hạn (gọi bởi background job). */
    @Query("UPDATE book_loans SET status = 'overdue' WHERE status = 'borrowed' AND dueDate < :currentDate")
    void markOverdueLoans(long currentDate);

    /** Kiểm tra cuốn sách có đang được mượn không. */
    @Query("SELECT COUNT(*) FROM book_loans WHERE bookId = :bookId AND status = 'borrowed'")
    int isBookCurrentlyLoaned(int bookId);
}
