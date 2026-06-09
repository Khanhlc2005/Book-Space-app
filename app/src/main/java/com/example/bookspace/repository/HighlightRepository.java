package com.example.bookspace.repository;

import android.content.Context;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.HighlightDao;
import com.example.bookspace.database.entity.Highlight;

import java.util.List;

public class HighlightRepository {
    private final HighlightDao highlightDao;

    public HighlightRepository(Context context) {
        highlightDao = AppDatabase.getInstance(context).highlightDao();
    }

    public List<Highlight> getHighlightsForBook(int bookId) {
        return highlightDao.getHighlightsForBook(bookId);
    }

    public long addHighlightSync(Highlight highlight) {
        return highlightDao.insertHighlight(highlight);
    }

    public void addHighlight(Highlight highlight) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            highlightDao.insertHighlight(highlight);
        });
    }

    public void deleteHighlight(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            highlightDao.deleteHighlight(id);
        });
    }
}
