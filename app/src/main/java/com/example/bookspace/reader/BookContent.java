package com.example.bookspace.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class chứa kết quả parse từ file sách .txt
 */
public class BookContent {

    private final List<Chapter> chapters;

    public BookContent(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public int getChapterCount() {
        return chapters.size();
    }

    /**
     * Lấy danh sách tên chương (dùng cho TocAdapter)
     */
    public List<String> getChapterNames() {
        List<String> names = new ArrayList<>();
        for (Chapter chapter : chapters) {
            names.add(chapter.getTitle());
        }
        return names;
    }

    /**
     * Một chương sách, chứa tiêu đề và danh sách đoạn văn
     */
    public static class Chapter {
        private final String title;
        private final List<Paragraph> paragraphs;

        public Chapter(String title, List<Paragraph> paragraphs) {
            this.title = title;
            this.paragraphs = paragraphs;
        }

        public String getTitle() {
            return title;
        }

        public List<Paragraph> getParagraphs() {
            return paragraphs;
        }
    }

    /**
     * Một đoạn văn trong chương sách
     */
    public static class Paragraph {
        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_QUOTE = 1;

        private final String text;
        private final int type;

        public Paragraph(String text, int type) {
            this.text = text;
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public int getType() {
            return type;
        }
    }
}
