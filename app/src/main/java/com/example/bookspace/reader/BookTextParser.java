package com.example.bookspace.reader;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Trình phân tích file sách .txt thành cấu trúc BookContent.
 *
 * Format quy ước:
 * - Dòng bắt đầu bằng "## " → Đầu chương mới, text sau "## " là tên chương
 * - Dòng bắt đầu bằng "> "  → Trích dẫn nổi bật (pull quote)
 * - Dòng trống              → Ngăn cách đoạn văn
 * - Mọi dòng khác           → Nội dung đoạn văn thường
 */
public class BookTextParser {

    /**
     * Parse file sách từ assets
     *
     * @param context  Context ứng dụng
     * @param filePath Đường dẫn file trong assets (VD: "books/dac_nhan_tam.txt")
     * @return BookContent chứa danh sách chương và đoạn văn, hoặc null nếu lỗi
     */
    public static BookContent parse(Context context, String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        AssetManager assetManager = context.getAssets();
        List<BookContent.Chapter> chapters = new ArrayList<>();

        try (InputStream is = assetManager.open(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            String currentChapterTitle = null;
            List<BookContent.Paragraph> currentParagraphs = new ArrayList<>();
            StringBuilder paragraphBuffer = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("## ")) {
                    // Gặp đầu chương mới → Flush chương cũ (nếu có)
                    flushParagraphBuffer(paragraphBuffer, currentParagraphs);
                    if (currentChapterTitle != null && !currentParagraphs.isEmpty()) {
                        chapters.add(new BookContent.Chapter(currentChapterTitle, currentParagraphs));
                    }

                    // Bắt đầu chương mới
                    currentChapterTitle = line.substring(3).trim();
                    currentParagraphs = new ArrayList<>();

                } else if (line.startsWith("> ")) {
                    // Trích dẫn (pull quote) → Flush buffer trước, rồi thêm quote
                    flushParagraphBuffer(paragraphBuffer, currentParagraphs);
                    String quoteText = line.substring(2).trim();
                    if (!quoteText.isEmpty()) {
                        currentParagraphs.add(new BookContent.Paragraph(quoteText, BookContent.Paragraph.TYPE_QUOTE));
                    }

                } else if (line.trim().isEmpty()) {
                    // Dòng trống → Flush paragraph buffer hiện tại
                    flushParagraphBuffer(paragraphBuffer, currentParagraphs);

                } else {
                    // Dòng text thường → Nối vào buffer
                    if (paragraphBuffer.length() > 0) {
                        paragraphBuffer.append(" ");
                    }
                    paragraphBuffer.append(line.trim());
                }
            }

            // Flush đoạn văn cuối cùng và chương cuối cùng
            flushParagraphBuffer(paragraphBuffer, currentParagraphs);
            if (currentChapterTitle != null && !currentParagraphs.isEmpty()) {
                chapters.add(new BookContent.Chapter(currentChapterTitle, currentParagraphs));
            } else if (currentChapterTitle == null && !currentParagraphs.isEmpty()) {
                // File không có dòng ## nào → toàn bộ là 1 chương
                chapters.add(new BookContent.Chapter("Nội dung", currentParagraphs));
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (chapters.isEmpty()) {
            return null;
        }

        // Post-process to calculate character offsets and indexes for paragraphs in each chapter
        for (int chIdx = 0; chIdx < chapters.size(); chIdx++) {
            BookContent.Chapter chapter = chapters.get(chIdx);
            int currentOffset = 0;
            List<BookContent.Paragraph> paras = chapter.getParagraphs();
            for (int pIdx = 0; pIdx < paras.size(); pIdx++) {
                BookContent.Paragraph p = paras.get(pIdx);
                p.setChapterIndex(chIdx);
                p.setParagraphIndex(pIdx);
                p.setCharacterOffsetInChapter(currentOffset);
                // Advance the offset for the next paragraph (length of current paragraph text + 1 char separator)
                currentOffset += p.getText().length() + 1;
            }
        }

        return new BookContent(chapters);
    }

    /**
     * Flush nội dung trong paragraphBuffer thành 1 Paragraph (TYPE_NORMAL)
     * và thêm vào danh sách. Sau đó xoá buffer.
     */
    private static void flushParagraphBuffer(StringBuilder buffer, List<BookContent.Paragraph> paragraphs) {
        if (buffer.length() > 0) {
            paragraphs.add(new BookContent.Paragraph(buffer.toString(), BookContent.Paragraph.TYPE_NORMAL));
            buffer.setLength(0);
        }
    }
}
