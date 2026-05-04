package com.example.bookspace.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.bookspace.database.dao.BookDao;
import com.example.bookspace.database.dao.FavouriteDao;
import com.example.bookspace.database.dao.ReadingProgressDao;
import com.example.bookspace.database.dao.ReadingSettingsDao;
import com.example.bookspace.database.dao.ReminderDao;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.FavouriteEntity;
import com.example.bookspace.database.entity.ReadingProgressEntity;
import com.example.bookspace.database.entity.ReadingSettingsEntity;
import com.example.bookspace.database.entity.ReminderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        BookEntity.class,
        FavouriteEntity.class,
        ReadingProgressEntity.class,
        ReadingSettingsEntity.class,
        ReminderEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BookDao bookDao();
    public abstract FavouriteDao favouriteDao();
    public abstract ReadingProgressDao readingProgressDao();
    public abstract ReadingSettingsDao readingSettingsDao();
    public abstract ReminderDao reminderDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "bookspace_room_db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                BookDao dao = INSTANCE.bookDao();
                List<BookEntity> sampleBooks = new ArrayList<>();
                sampleBooks.add(createBook("Trưởng Thành Sau Ngàn Lần Tranh Đấu", "Rando Kim", "https://picsum.photos/600/400?random=101", 300, "Mô tả 1", "KỸ NĂNG SỐNG"));
                sampleBooks.add(createBook("Một Thoáng Ta Rực Rỡ Ở Nhân Gian", "Ocean Vuong", "https://picsum.photos/600/400?random=102", 350, "Mô tả 2", "TIỂU THUYẾT"));
                sampleBooks.add(createBook("Thiên Tài Bên Trái, Kẻ Điên Bên Phải", "Cao Minh", "https://picsum.photos/600/400?random=103", 400, "Mô tả 3", "TÂM LÝ HỌC"));
                sampleBooks.add(createBook("Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", "https://picsum.photos/600/400?random=104", 250, "Mô tả 4", "KỸ NĂNG SỐNG"));
                sampleBooks.add(createBook("Dám Bị Ghét", "Kishimi Ichiro", "https://picsum.photos/600/400?random=105", 320, "Mô tả 5", "TÂM LÝ"));
                sampleBooks.add(createBook("Đắc Nhân Tâm", "Dale Carnegie", "https://picsum.photos/600/400?random=106", 320, "Sách kỹ năng sống hay nhất...", "KỸ NĂNG SỐNG"));
                
                dao.insertAll(sampleBooks);
            });
        }
    };

    private static BookEntity createBook(String title, String author, String cover, int pages, String desc, String cat) {
        BookEntity b = new BookEntity();
        b.title = title;
        b.author = author;
        b.coverUrl = cover;
        b.pages = pages;
        b.description = desc;
        b.category = cat;
        return b;
    }
}
