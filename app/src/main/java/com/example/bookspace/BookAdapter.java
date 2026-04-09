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

    public BookAdapter(List<Book> list) {
        this.list = list;
        this.isSearchMode = false;
    }

    public BookAdapter(List<Book> list, boolean isSearchMode) {
        this.list = list;
        this.isSearchMode = isSearchMode;
    }

    public void updateData(List<Book> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isSearchMode) {
            // Layout "như lúc trước" cho tìm kiếm
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v, true);
        } else {
            // Layout dạng lưới có hình ảnh cho các Category
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_book_rview, parent, false);
            return new ViewHolder(v, false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book b = list.get(position);
        holder.title.setText(b.getTitle());
        holder.author.setText(b.getAuthor());

        if (!isSearchMode && holder.imgCover != null) {
            // Load ảnh bìa bằng Glide (chỉ cho Grid View)
            if (b.getCoverUrl() != null && !b.getCoverUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(b.getCoverUrl())
                        .transform(new CenterCrop(), new RoundedCorners(24))
                        .into(holder.imgCover);
            } else {
                holder.imgCover.setImageResource(R.drawable.book_cover_bg);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
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