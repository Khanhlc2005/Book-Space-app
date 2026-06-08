package com.example.bookspace.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.BookLoanDao;
import com.example.bookspace.database.entity.BookLoanEntity;

import java.util.List;

public class BookLoanRepository {
    private final BookLoanDao bookLoanDao;

    public BookLoanRepository(Context context) {
        bookLoanDao = AppDatabase.getInstance(context).bookLoanDao();
    }

    // ── Đọc (LiveData cho UI observe) ──────────────────────────────

    public LiveData<List<BookLoanEntity>> getAllLoans() {
        return bookLoanDao.getAllLoans();
    }

    public LiveData<List<BookLoanEntity>> getActiveLoans() {
        return bookLoanDao.getActiveLoans();
    }

    public LiveData<List<BookLoanEntity>> getOverdueLoans(long currentDate) {
        return bookLoanDao.getOverdueLoans(currentDate);
    }

    // ── Ghi (chạy trên background thread) ──────────────────────────

    /** Tạo giao dịch mượn sách mới. */
    public void insertLoan(BookLoanEntity loan) {
        AppDatabase.databaseWriteExecutor.execute(() -> bookLoanDao.insertLoan(loan));
    }

    /** Đánh dấu đã trả sách. */
    public void markAsReturned(int loanId, long returnDate) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                bookLoanDao.markAsReturned(loanId, returnDate));
    }

    /** Quét và đánh dấu các giao dịch quá hạn. */
    public void markOverdueLoans(long currentDate) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                bookLoanDao.markOverdueLoans(currentDate));
    }

    /** Kiểm tra cuốn sách có đang được mượn không (đồng bộ). */
    public boolean isBookCurrentlyLoaned(int bookId) {
        return bookLoanDao.isBookCurrentlyLoaned(bookId) > 0;
    }
}
