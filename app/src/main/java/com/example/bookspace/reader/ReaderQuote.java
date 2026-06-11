package com.example.bookspace.reader;

public class ReaderQuote {
    private final String text;
    private final String bookTitle;
    private final String authorName;
    private final String chapterName;
    private final int chapterIndex;
    private final int pageNumber;

    public ReaderQuote(String text, String chapterName, int chapterIndex, int pageNumber) {
        this(text, "", "", chapterName, chapterIndex, pageNumber);
    }

    public ReaderQuote(String text, String bookTitle, String authorName,
                       String chapterName, int chapterIndex, int pageNumber) {
        this.text = text;
        this.bookTitle = bookTitle;
        this.authorName = authorName;
        this.chapterName = chapterName;
        this.chapterIndex = chapterIndex;
        this.pageNumber = pageNumber;
    }

    public String getText() {
        return text;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getChapterName() {
        return chapterName;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
