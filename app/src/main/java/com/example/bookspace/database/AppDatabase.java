package com.example.bookspace.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.migration.Migration;
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
    version = 3,
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
                            .allowMainThreadQueries() // Mặc định cho phép học tập/Đồ án
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigration() // Tự xóa DB cũ khi thay đổi version
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
                sampleBooks.add(createBook("Trưởng Thành Sau Ngàn Lần Tranh Đấu", "Rando Kim", "https://picsum.photos/600/400?random=101", 300, "Hành trình trưởng thành qua những thử thách cuộc sống.", "KỸ NĂNG SỐNG"));
                sampleBooks.add(createBook("Một Thoáng Ta Rực Rỡ Ở Nhân Gian", "Ocean Vuong", "https://picsum.photos/600/400?random=102", 350, "Câu chuyện đầy chất thơ về gia đình, ký ức và bản sắc.", "TIỂU THUYẾT"));
                sampleBooks.add(createBook("Thiên Tài Bên Trái, Kẻ Điên Bên Phải", "Cao Minh", "https://picsum.photos/600/400?random=103", 400, "Ranh giới mong manh giữa thiên tài và kẻ điên.", "TÂM LÝ"));
                sampleBooks.add(createBook("Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", "https://picsum.photos/600/400?random=104", 250, "Những bài học quý giá cho tuổi trẻ Việt Nam.", "KỸ NĂNG SỐNG"));
                sampleBooks.add(createBook("Dám Bị Ghét", "Kishimi Ichiro", "https://picsum.photos/600/400?random=105", 320, "Triết lý Adler về sự tự do và hạnh phúc.", "TÂM LÝ"));
                sampleBooks.add(createBook("Đắc Nhân Tâm", "Dale Carnegie", "https://picsum.photos/600/400?random=106", 320, "Sách kỹ năng sống kinh điển về nghệ thuật giao tiếp.", "KỸ NĂNG SỐNG"));
                sampleBooks.add(createBook("Harry Potter và Hòn Đá Phù Thủy", "J.K. Rowling", "https://picsum.photos/600/400?random=107", 500, "Thế giới phù thủy kỳ bí cùng cậu bé Harry.", "TIỂU THUYẾT"));
                sampleBooks.add(createBook("Nhà Giả Kim", "Paulo Coelho", "https://picsum.photos/600/400?random=108", 200, "Hành trình tìm kiếm vận mệnh của chàng chăn cừu Santiago.", "TIỂU THUYẾT"));
                sampleBooks.add(createBook("Tư Duy Nhanh Và Chậm", "Daniel Kahneman", "https://picsum.photos/600/400?random=109", 500, "Hai hệ thống tư duy chi phối quyết định của con người.", "TÂM LÝ"));
                sampleBooks.add(createBook("Muôn Kiếp Nhân Sinh", "Nguyên Phong", "https://picsum.photos/600/400?random=110", 450, "Luân hồi và nhân quả qua các kiếp sống.", "TÂM LINH"));
                sampleBooks.add(createBook("Sapiens: Lược Sử Loài Người", "Yuval Noah Harari", "https://picsum.photos/600/400?random=111", 550, "Lịch sử 70.000 năm phát triển của loài người.", "KINH TẾ"));
                sampleBooks.add(createBook("Chúa Tể Nhẫn", "J.R.R. Tolkien", "https://picsum.photos/600/400?random=112", 1200, "Cuộc chiến giành chiếc nhẫn quyền năng ở Middle-earth.", "TIỂU THUYẾT"));
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
        b.isDownloaded = false;
        return b;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `isActive` INTEGER NOT NULL)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE books ADD COLUMN isDownloaded INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_books_title_author ON books(title, author)");
        }
    };
}
