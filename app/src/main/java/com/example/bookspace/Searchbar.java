package com.example.bookspace;

import java.util.ArrayList;
import java.util.List;

public class Searchbar {

    public static List<Book> filter(List<Book> list, String keyword) {
        List<Book> result = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            result.addAll(list);
            return result;
        }

        keyword = keyword.toLowerCase();

        for (Book b : list) {
            if (b.getTitle().toLowerCase().contains(keyword)
                    || b.getAuthor().toLowerCase().contains(keyword)) {
                result.add(b);
            }
        }

        return result;
    }
}