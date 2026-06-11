package com.example.bookspace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Searchbar {

    public static List<Book> filter(List<Book> list, String keyword) {
        List<Book> result = new ArrayList<>();
        if (list == null) {
            return result;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            result.addAll(list);
            return result;
        }

        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);

        for (Book b : list) {
            if (b == null) {
                continue;
            }
            String title = safeText(b.getTitle()).toLowerCase(Locale.ROOT);
            String author = safeText(b.getAuthor()).toLowerCase(Locale.ROOT);
            if (title.contains(normalizedKeyword) || author.contains(normalizedKeyword)) {
                result.add(b);
            }
        }

        return result;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }
}
