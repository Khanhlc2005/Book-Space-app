package com.example.bookspace;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BookSpaceDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookspace.db";
    private static final int DATABASE_VERSION = 1;

    // --- Bảng Books ---
    private static final String TABLE_BOOKS = "books";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AUTHOR = "author";
    private static final String COLUMN_COVER_URL = "coverUrl";
    private static final String COLUMN_PAGES = "pages";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";

    // --- Bảng Favorites ---
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_FAV_ID = "id";
    private static final String COLUMN_FAV_BOOK_ID = "book_id";

    // --- Bảng Reading Progress ---
    private static final String TABLE_READING_PROGRESS = "reading_progress";
    private static final String COLUMN_PROG_ID = "id";
    private static final String COLUMN_PROG_BOOK_ID = "book_id";
    private static final String COLUMN_PROG_CURRENT_PAGE = "current_page";
    private static final String COLUMN_PROG_LAST_READ = "last_read_time";

    public BookSpaceDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng sách (books)
        String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_AUTHOR + " TEXT,"
                + COLUMN_COVER_URL + " TEXT,"
                + COLUMN_PAGES + " INTEGER,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT" + ")";
        db.execSQL(CREATE_BOOKS_TABLE);

        // Bảng yêu thích (favorites)
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FAV_BOOK_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_FAV_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_FAVORITES_TABLE);

        // Bảng tiến độ đọc (reading_progress)
        String CREATE_READING_PROGRESS_TABLE = "CREATE TABLE " + TABLE_READING_PROGRESS + "("
                + COLUMN_PROG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PROG_BOOK_ID + " INTEGER,"
                + COLUMN_PROG_CURRENT_PAGE + " INTEGER,"
                + COLUMN_PROG_LAST_READ + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_PROG_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_READING_PROGRESS_TABLE);

        // Chèn dữ liệu mẫu 6 cuốn sách
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_READING_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        // Create tables again
        onCreate(db);
    }

    /**
     * Thực hiện Seed Data cho 6 cuốn sách mẫu
     */
    private void seedData(SQLiteDatabase db) {
        Book[] sampleBooks = {
            new Book("https://picsum.photos/600/400?random=101", "Trưởng Thành Sau Ngàn Lần Tranh Đấu", "Rando Kim", 300, "Mô tả 1", "KỸ NĂNG SỐNG"),
            new Book("https://picsum.photos/600/400?random=102", "Một Thoáng Ta Rực Rỡ Ở Nhân Gian", "Ocean Vuong", 350, "Mô tả 2", "TIỂU THUYẾT"),
            new Book("https://picsum.photos/600/400?random=103", "Thiên Tài Bên Trái, Kẻ Điên Bên Phải", "Cao Minh", 400, "Mô tả 3", "TÂM LÝ HỌC"),
            new Book("https://picsum.photos/600/400?random=104", "Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", 250, "Mô tả 4", "KỸ NĂNG SỐNG"),
            new Book("https://picsum.photos/600/400?random=105", "Dám Bị Ghét", "Kishimi Ichiro", 320, "Mô tả 5", "TÂM LÝ"),
            new Book("https://picsum.photos/600/400?random=106", "Đắc Nhân Tâm", "Dale Carnegie", 320, "Sách kỹ năng sống hay nhất...", "KỸ NĂNG SỐNG")
        };

        for (Book b : sampleBooks) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, b.getTitle());
            values.put(COLUMN_AUTHOR, b.getAuthor());
            values.put(COLUMN_COVER_URL, b.getCoverUrl());
            values.put(COLUMN_PAGES, b.getPages());
            values.put(COLUMN_DESCRIPTION, b.getDescription());
            values.put(COLUMN_CATEGORY, b.getCategory());
            // Insert data vào bảng books
            db.insert(TABLE_BOOKS, null, values);
        }
    }

    // ====================================================================
    // CRUD 
    // ====================================================================

    /**
     * Thêm sách mới 
     */
    public void addBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_COVER_URL, book.getCoverUrl());
        values.put(COLUMN_PAGES, book.getPages());
        values.put(COLUMN_DESCRIPTION, book.getDescription());
        values.put(COLUMN_CATEGORY, book.getCategory());
        db.insert(TABLE_BOOKS, null, values);
        db.close();
    }

    /**
     * Lấy toàn bộ sách trong thư viện
     */
    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BOOKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
                String coverUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COVER_URL));
                int pages = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGES));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));

                Book book = new Book(coverUrl, title, author, pages, description, category);
                bookList.add(book);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookList;
    }

    /**
     * Lấy danh sách sách lọc theo "Danh Mục"
     */
    public List<Book> getBooksByCategory(String categoryParams) {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS, 
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_AUTHOR, COLUMN_COVER_URL, COLUMN_PAGES, COLUMN_DESCRIPTION, COLUMN_CATEGORY},
                COLUMN_CATEGORY + "=?", 
                new String[]{categoryParams}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
                String coverUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COVER_URL));
                int pages = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGES));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));

                Book book = new Book(coverUrl, title, author, pages, description, category);
                bookList.add(book);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookList;
    }

    /**
     * Chức năng tìm kiếm SQL (Tìm Tên Sách gần giống Từ khóa)
     */
    public List<Book> searchBooks(String keyword) {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String searchQuery = "SELECT * FROM " + TABLE_BOOKS + " WHERE " + COLUMN_TITLE + " LIKE ?";
        Cursor cursor = db.rawQuery(searchQuery, new String[]{"%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
                String coverUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COVER_URL));
                int pages = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGES));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));

                Book book = new Book(coverUrl, title, author, pages, description, category);
                bookList.add(book);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookList;
    }
}
