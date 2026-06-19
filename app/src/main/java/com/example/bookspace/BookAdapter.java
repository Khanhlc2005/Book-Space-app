package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private List<Book> list;
    private boolean isSearchMode; // true: list view (no image), false: grid view (with image)
    private boolean isGridMode;
    private OnBookClickListener listener;

    public BookAdapter(List<Book> list, OnBookClickListener listener) {
        this.list = list != null ? list : new java.util.ArrayList<>();
        this.isSearchMode = false;
        this.isGridMode = false;
        this.listener = listener;
    }

    public BookAdapter(List<Book> list, OnBookClickListener listener, boolean isGridMode) {
        this.list = list != null ? list : new java.util.ArrayList<>();
        this.isSearchMode = false;
        this.isGridMode = isGridMode;
        this.listener = listener;
    }

    public BookAdapter(List<Book> list, boolean isSearchMode, OnBookClickListener listener) {
        this.list = list != null ? list : new java.util.ArrayList<>();
        this.isSearchMode = isSearchMode;
        this.isGridMode = false;
        this.listener = listener;
    }

    public void updateData(List<Book> newList) {
        this.list = newList != null ? newList : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isSearchMode) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v, true);
        } else {
            // Layout dạng lưới có hình ảnh cho các Category
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(isGridMode ? R.layout.item_book_grid : R.layout.list_item_book_rview, parent, false);
            return new ViewHolder(v, false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book b = list.get(position);
        holder.title.setText(safeText(b == null ? null : b.getTitle()));
        holder.author.setText(safeText(b == null ? null : b.getAuthor()));

        if (!isSearchMode && holder.imgCover != null) {
            // Load ảnh bìa bằng Glide (chỉ cho Grid View)
            String coverUrl = b == null ? "" : safeText(b.getCoverUrl());
            if (!coverUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(coverUrl)
                        .transform(new CenterCrop(), new RoundedCorners(24))
                        .into(holder.imgCover);
            } else {
                holder.imgCover.setImageResource(R.drawable.book_cover_bg);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && b != null) {
                listener.onBookClick(b);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView title, author;

        public ViewHolder(@NonNull View itemView, boolean isSearch) {
            super(itemView);
            if (isSearch) {
                // IDs mặc định của simple_list_item_2
                title = itemView.findViewById(android.R.id.text1);
                author = itemView.findViewById(android.R.id.text2);
            } else {
                // IDs trong layout tùy chỉnh list_item_book_rview
                imgCover = itemView.findViewById(R.id.imgBookCover);
                title = itemView.findViewById(R.id.tvBookTitle);
                author = itemView.findViewById(R.id.tvBookAuthor);
            }
        }
    }
}
