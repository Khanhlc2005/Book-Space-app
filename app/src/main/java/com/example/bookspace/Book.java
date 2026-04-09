package com.example.bookspace;

public class Book {
    private int id;
    private String title;
    private String author;
    private String status;
    private int coverResId; // Mã ID của ảnh trong thư mục res/drawable hoặc res/mipmap

    // Constructor để khởi tạo dữ liệu
    public Book(int id, String title, String author, String status, int coverResId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
        this.coverResId = coverResId;
    }

    // Các hàm Getter để lấy dữ liệu ra
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getStatus() { return status; }
    public int getCoverResId() { return coverResId; }
}
