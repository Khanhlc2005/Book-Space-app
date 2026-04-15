package com.example.bookspace;

public class Book {
    private int id; // ID định danh (tự động phát sinh từ DB)
    private String title;
    private String author;
    private String coverUrl; 
    private int pages;       
    private String description;
    private String category;

    // Constructor 2 tham số (của nhánh main)
    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    // Constructor 3 tham số (của nhánh main)
    public Book(String title, String author, String coverUrl) {
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
    }

    // Constructor 5 tham số (của nhánh main - dùng cho chức năng Search)
    public Book(String coverUrl, String title, String author, int pages, String category) {
        this.coverUrl = coverUrl;
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.category = category;
    }

    // Constructor 6 tham số (dùng cho chức năng Preview Slider)
    public Book(String coverUrl, String title, String author, int pages, String description, String category) {
        this.coverUrl = coverUrl;
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.description = description;
        this.category = category;
    }

    // Các hàm Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCoverUrl() {
        return coverUrl;
    }
    
    public String getImageUrl() {
        return coverUrl;
    }

    public int getPages() {
        return pages;
    }
    
    public int getPageCount() {
        return pages;
    }

    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }

    public String getCategory() { 
        return category; 
    }
    
    public void setCategory(String category) { 
        this.category = category; 
    }

    @Override
    public String toString() {
        return title + " - " + author;
    }
}
