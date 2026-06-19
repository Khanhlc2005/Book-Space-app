package com.example.bookspace.database;

import android.content.Context;
import android.database.Cursor;

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
import com.example.bookspace.database.dao.ReviewDao;
import com.example.bookspace.database.dao.BookLoanDao;
import com.example.bookspace.database.dao.ChallengeDao;
import com.example.bookspace.database.dao.HighlightDao;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.BookLoanEntity;
import com.example.bookspace.database.entity.ChallengeEntity;
import com.example.bookspace.database.entity.FavouriteEntity;
import com.example.bookspace.database.entity.Highlight;
import com.example.bookspace.database.entity.ReadingProgressEntity;
import com.example.bookspace.database.entity.ReadingSettingsEntity;
import com.example.bookspace.database.entity.ReminderEntity;
import com.example.bookspace.database.entity.ReviewEntity;

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
        ReminderEntity.class,
        ReviewEntity.class,
        BookLoanEntity.class,
        ChallengeEntity.class,
        Highlight.class
    },
    version = 8,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BookDao bookDao();
    public abstract FavouriteDao favouriteDao();
    public abstract ReadingProgressDao readingProgressDao();
    public abstract ReadingSettingsDao readingSettingsDao();
    public abstract ReminderDao reminderDao();
    public abstract ReviewDao reviewDao();
    public abstract BookLoanDao bookLoanDao();
    public abstract ChallengeDao challengeDao();
    public abstract HighlightDao highlightDao();

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
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                                    MIGRATION_4_5, MIGRATION_5_6, MIGRATION_7_8)
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
                sampleBooks.add(createBook("Trưởng Thành Sau Ngàn Lần Tranh Đấu", "Rando Kim", "https://picsum.photos/600/400?random=101", 300, "Hành trình trưởng thành qua những thử thách cuộc sống.", "KỸ NĂNG SỐNG", null));
                sampleBooks.add(createBook("Một Thoáng Ta Rực Rỡ Ở Nhân Gian", "Ocean Vuong", "https://picsum.photos/600/400?random=102", 350, "Câu chuyện đầy chất thơ về gia đình, ký ức và bản sắc.", "TIỂU THUYẾT", null));
                sampleBooks.add(createBook("Thiên Tài Bên Trái, Kẻ Điên Bên Phải", "Cao Minh", "https://picsum.photos/600/400?random=103", 400, "Ranh giới mong manh giữa thiên tài và kẻ điên.", "TÂM LÝ", null));
                sampleBooks.add(createBook("Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", "https://picsum.photos/600/400?random=104", 250, "Những bài học quý giá cho tuổi trẻ Việt Nam.", "KỸ NĂNG SỐNG", null));
                sampleBooks.add(createBook("Dám Bị Ghét", "Kishimi Ichiro", "https://picsum.photos/600/400?random=105", 320, "Triết lý Adler về sự tự do và hạnh phúc.", "TÂM LÝ", null));
                sampleBooks.add(createBook("Đắc Nhân Tâm", "Dale Carnegie", "https://picsum.photos/600/400?random=106", 320, "Sách kỹ năng sống kinh điển về nghệ thuật giao tiếp.", "KỸ NĂNG SỐNG", "books/dac_nhan_tam.txt"));
                sampleBooks.add(createBook("Harry Potter và Hòn Đá Phù Thủy", "J.K. Rowling", "https://picsum.photos/600/400?random=107", 500, "Thế giới phù thủy kỳ bí cùng cậu bé Harry.", "TIỂU THUYẾT", null));
                sampleBooks.add(createBook("Nhà Giả Kim", "Paulo Coelho", "https://picsum.photos/600/400?random=108", 200, "Hành trình tìm kiếm vận mệnh của chàng chăn cừu Santiago.", "TIỂU THUYẾT", "books/nha_gia_kim.txt"));
                sampleBooks.add(createBook("Tư Duy Nhanh Và Chậm", "Daniel Kahneman", "https://picsum.photos/600/400?random=109", 500, "Hai hệ thống tư duy chi phối quyết định của con người.", "TÂM LÝ", null));
                sampleBooks.add(createBook("Muôn Kiếp Nhân Sinh", "Nguyên Phong", "https://picsum.photos/600/400?random=110", 450, "Luân hồi và nhân quả qua các kiếp sống.", "TÂM LINH", null));
                sampleBooks.add(createBook("Sapiens: Lược Sử Loài Người", "Yuval Noah Harari", "https://picsum.photos/600/400?random=111", 550, "Lịch sử 70.000 năm phát triển của loài người.", "KINH TẾ", null));
                sampleBooks.add(createBook("Chúa Tể Nhẫn", "J.R.R. Tolkien", "https://picsum.photos/600/400?random=112", 1200, "Cuộc chiến giành chiếc nhẫn quyền năng ở Middle-earth.", "TIỂU THUYẾT", null));
                dao.insertAll(sampleBooks);
            });
        }
    };

    private static BookEntity createBook(String title, String author, String cover, int pages, String desc, String cat, String bookFilePath) {
        BookEntity b = new BookEntity();
        b.title = title;
        b.author = author;
        b.coverUrl = cover;
        b.pages = pages;
        b.description = desc;
        b.category = cat;
        b.isDownloaded = false;
        b.bookFilePath = bookFilePath;
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

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE books ADD COLUMN bookFilePath TEXT");
            // Cập nhật bookFilePath cho 2 cuốn sách có sẵn nội dung
            database.execSQL("UPDATE books SET bookFilePath = 'books/dac_nhan_tam.txt' WHERE title = 'Đắc Nhân Tâm'");
            database.execSQL("UPDATE books SET bookFilePath = 'books/nha_gia_kim.txt' WHERE title = 'Nhà Giả Kim'");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `reviews` (" +
                    "`userId` TEXT NOT NULL, `bookId` INTEGER NOT NULL, `rating` INTEGER NOT NULL, " +
                    "`content` TEXT, `createdAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`userId`, `bookId`))");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Bảng book_loans – mượn sách (FK → books.id)
            database.execSQL("CREATE TABLE IF NOT EXISTS `book_loans` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`bookId` INTEGER NOT NULL, " +
                    "`borrowDate` INTEGER NOT NULL DEFAULT 0, " +
                    "`dueDate` INTEGER NOT NULL DEFAULT 0, " +
                    "`returnDate` INTEGER NOT NULL DEFAULT 0, " +
                    "`status` TEXT, " +
                    "FOREIGN KEY(`bookId`) REFERENCES `books`(`id`) ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_book_loans_bookId` ON `book_loans` (`bookId`)");
            // Bảng challenges – thử thách đọc sách
            database.execSQL("CREATE TABLE IF NOT EXISTS `challenges` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT, " +
                    "`challengeType` TEXT, " +
                    "`bookId` INTEGER NOT NULL DEFAULT 0, " +
                    "`targetValue` INTEGER NOT NULL DEFAULT 0, " +
                    "`currentValue` INTEGER NOT NULL DEFAULT 0, " +
                    "`startDate` INTEGER NOT NULL DEFAULT 0, " +
                    "`endDate` INTEGER NOT NULL DEFAULT 0, " +
                    "`status` TEXT)");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `highlights` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`bookId` INTEGER NOT NULL, " +
                    "`highlightedText` TEXT, " +
                    "`chapterName` TEXT, " +
                    "`chapterIndex` INTEGER NOT NULL, " +
                    "`characterOffsetStart` INTEGER NOT NULL, " +
                    "`bookTitle` TEXT, " +
                    "`authorName` TEXT, " +
                    "`pageNumber` INTEGER NOT NULL DEFAULT -1, " +
                    "`paragraphIndex` INTEGER NOT NULL DEFAULT -1, " +
                    "`createdAt` INTEGER NOT NULL DEFAULT 0)");
            addColumnIfMissing(database, "highlights", "bookTitle", "TEXT");
            addColumnIfMissing(database, "highlights", "authorName", "TEXT");
            addColumnIfMissing(database, "highlights", "pageNumber", "INTEGER NOT NULL DEFAULT -1");
            addColumnIfMissing(database, "highlights", "paragraphIndex", "INTEGER NOT NULL DEFAULT -1");
            addColumnIfMissing(database, "highlights", "createdAt", "INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static void addColumnIfMissing(@NonNull SupportSQLiteDatabase database,
                                           @NonNull String tableName,
                                           @NonNull String columnName,
                                           @NonNull String columnDefinition) {
        if (columnExists(database, tableName, columnName)) {
            return;
        }
        database.execSQL("ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnDefinition);
    }

    private static boolean columnExists(@NonNull SupportSQLiteDatabase database,
                                        @NonNull String tableName,
                                        @NonNull String columnName) {
        try (Cursor cursor = database.query("PRAGMA table_info(`" + tableName + "`)")) {
            int nameColumnIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (nameColumnIndex >= 0 && columnName.equals(cursor.getString(nameColumnIndex))) {
                    return true;
                }
            }
        }
        return false;
    }
}
