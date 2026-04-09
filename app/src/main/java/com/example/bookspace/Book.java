package com.example.bookspace;

public class Book {
    private String title;
    private String author;
    private String coverUrl;
    private int pages;
    private String category;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public Book(String title, String author, String coverUrl) {
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
    }

    public Book(String coverUrl, String title, String author, int pages, String category) {
        this.coverUrl = coverUrl;
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.category = category;
    }

    // Các hàm Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public int getPages() {
        return pages;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return title + " - " + author;
    }
}