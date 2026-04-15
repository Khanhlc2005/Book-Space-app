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
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.FavouriteEntity;
import com.example.bookspace.database.entity.ReadingProgressEntity;
import com.example.bookspace.database.entity.ReadingSettingsEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hướng dẫn sử dụng AppDatabase:
 * 
 * 1. Khởi tạo và lấy instance của Database (Thường gọi trong Activity / Fragment / Repository):
 *    AppDatabase db = AppDatabase.getInstance(context);
 * 
 * 2. Lấy các DAO (Data Access Object) tương ứng để thao tác với các bảng:
 *    BookDao bookDao = db.bookDao();
 *    FavouriteDao favouriteDao = db.favouriteDao();
 *    ReadingProgressDao progressDao = db.readingProgressDao();
 *    ReadingSettingsDao settingsDao = db.readingSettingsDao();
 * 
 * 3. Thực hiện thao tác với dữ liệu (CRUD - Create, Read, Update, Delete):
 *    - Đọc dữ liệu: List<BookEntity> books = bookDao.getAllBooks();
 *    - Thêm dữ liệu: bookDao.insert(new BookEntity(...));
 *    - Cập nhật tiến độ đọc: progressDao.insertOrUpdate(new ReadingProgressEntity(...));
 * 
 * *** LƯU Ý QUAN TRỌNG VỀ HIỆU SUẤT (Threading) ***
 * - Project hiện tại đang gọi `.allowMainThreadQueries()` để ĐƠN GIẢN HOÁ quá trình học / làm đồ án.
 *   Nghĩa là bạn CÓ THỂ gọi thao tác CSDL trực tiếp trên Main Thread.
 * - Tuу nhiên, trong thực tế (Best Practice), thao tác DB mất thời gian và có thể làm lag giao diện (ANR).
 * - Khuyến khích sử dụng `AppDatabase.databaseWriteExecutor` (đã khai báo ở dưới) cho các thao tác đổi dữ liệu (Insert/Update/Delete).
 * 
 * Ví dụ chạy dưới luồng nền (Background thread):
 * AppDatabase.databaseWriteExecutor.execute(() -> {
 *     db.bookDao().insert(newBook);
 * });
 */
@Database(
    entities = {
        BookEntity.class,
        FavouriteEntity.class,
        ReadingProgressEntity.class,
        ReadingSettingsEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BookDao bookDao();
    public abstract FavouriteDao favouriteDao();
    public abstract ReadingProgressDao readingProgressDao();
    public abstract ReadingSettingsDao readingSettingsDao();

    private static volatile AppDatabase INSTANCE;
    
    // Nơi chạy Thread nền chuẩn chỉnh theo Android (thay cho AsyncTask)
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "bookspace_room_db")
                            .allowMainThreadQueries() // Mặc định cho phép học tập/Đồ án
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
            // Dùng ExecutorService để gọi insert dữ liệu ở background thread mới ngầu 😎
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
