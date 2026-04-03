package com.example.bookspace;

public class Book {
    private String imageUrl;
    private String title;
    private String author;
    private int pageCount;
    private String description;
    private String category;

    public Book(String imageUrl, String title, String author, int pageCount, String description, String category) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.pageCount = pageCount;
        this.description = description;
        this.category = category;
    }

    // Getters and Setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
