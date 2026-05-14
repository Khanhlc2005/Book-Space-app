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

public class RelatedBookAdapter extends RecyclerView.Adapter<RelatedBookAdapter.ViewHolder> {
    private final List<Book> books;
    private final OnBookClickListener listener;

    public RelatedBookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_related_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());

        if (book.getCoverUrl() == null || book.getCoverUrl().isEmpty()) {
            holder.cover.setImageResource(R.drawable.book_cover_bg);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(book.getCoverUrl())
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .into(holder.cover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView title;
        final TextView author;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.imgRelatedCover);
            title = itemView.findViewById(R.id.txtRelatedTitle);
            author = itemView.findViewById(R.id.txtRelatedAuthor);
        }
    }
}
