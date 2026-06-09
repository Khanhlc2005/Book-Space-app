package com.example.bookspace.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bookspace.database.entity.Highlight;

import java.util.List;

@Dao
public interface HighlightDao {
    @Query("SELECT * FROM highlights WHERE bookId = :bookId")
    List<Highlight> getHighlightsForBook(int bookId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertHighlight(Highlight highlight);

    @Query("DELETE FROM highlights WHERE id = :id")
    void deleteHighlight(int id);
}
