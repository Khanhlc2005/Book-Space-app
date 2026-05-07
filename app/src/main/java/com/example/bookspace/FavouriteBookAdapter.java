package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookspace.database.entity.BookEntity;

import java.util.ArrayList;
import java.util.List;

public class FavouriteBookAdapter extends RecyclerView.Adapter<FavouriteBookAdapter.ViewHolder> {
    public interface Listener {
        void onBookClick(BookEntity book);
        void onRemoveFavourite(BookEntity book);
    }

    private final List<BookEntity> books = new ArrayList<>();
    private final Listener listener;

    public FavouriteBookAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<BookEntity> newBooks) {
        books.clear();
        if (newBooks != null) {
            books.addAll(newBooks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_fav, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookEntity book = books.get(position);
        holder.title.setText(book.title);
        holder.author.setText(book.author);
        holder.status.setText(book.isDownloaded ? "Đã tải về máy" : "Chưa tải về máy");

        if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
            Glide.with(holder.cover)
                    .load(book.coverUrl)
                    .centerCrop()
                    .into(holder.cover);
        } else {
            holder.cover.setImageResource(R.drawable.book_cover_bg);
        }

        holder.itemView.setOnClickListener(v -> listener.onBookClick(book));
        holder.removeFavourite.setOnClickListener(v -> listener.onRemoveFavourite(book));
        holder.delete.setOnClickListener(v -> listener.onRemoveFavourite(book));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView title;
        final TextView author;
        final TextView status;
        final ImageButton removeFavourite;
        final ImageButton delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.imgBookCover);
            title = itemView.findViewById(R.id.txtBookTitle);
            author = itemView.findViewById(R.id.txtAuthor);
            status = itemView.findViewById(R.id.txtReadingStatus);
            removeFavourite = itemView.findViewById(R.id.btnRemoveFavorite);
            delete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
