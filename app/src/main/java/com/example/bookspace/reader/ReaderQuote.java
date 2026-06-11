package com.example.bookspace.reader;

public class ReaderQuote {
    private final String text;
    private final String chapterName;
    private final int chapterIndex;
    private final int pageNumber;

    public ReaderQuote(String text, String chapterName, int chapterIndex, int pageNumber) {
        this.text = text;
        this.chapterName = chapterName;
        this.chapterIndex = chapterIndex;
        this.pageNumber = pageNumber;
    }

    public String getText() {
        return text;
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
