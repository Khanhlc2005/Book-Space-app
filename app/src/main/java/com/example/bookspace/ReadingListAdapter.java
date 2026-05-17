package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.bookspace.database.entity.ReadingProgressEntity;

import java.util.List;

/**
 * Adapter cho danh sách sách đang đọc (CurrentlyReadingListActivity).
 * Hiển thị ảnh bìa, tên sách, tác giả, tiến độ đọc (ProgressBar).
 */
public class ReadingListAdapter extends RecyclerView.Adapter<ReadingListAdapter.ViewHolder> {

    public interface OnReadingListActionListener {
        void onDeleteBook(Book book, int position);
        void onBookClick(Book book);
    }

    private List<Book> books;
    private List<ReadingProgressEntity> progressList;
    private final OnReadingListActionListener listener;

    public ReadingListAdapter(List<Book> books, List<ReadingProgressEntity> progressList, OnReadingListActionListener listener) {
        this.books = books;
        this.progressList = progressList;
        this.listener = listener;
    }

    public void updateData(List<Book> newBooks, List<ReadingProgressEntity> newProgress) {
        this.books = newBooks;
        this.progressList = newProgress;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < books.size()) {
            books.remove(position);
            if (progressList != null && position < progressList.size()) {
                progressList.remove(position);
            }
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, books.size());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_reading_booklist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);

        holder.txtTitle.setText(book.getTitle());
        holder.txtAuthor.setText(book.getAuthor());

        // Hiển thị tiến độ đọc
        if (progressList != null && position < progressList.size()) {
            ReadingProgressEntity progress = progressList.get(position);
            int current = progress.currentPage;
            int total = progress.totalPages;
            int percent = total > 0 ? (current * 100 / total) : 0;

            holder.txtStatus.setText("Đã đọc " + current + " / " + total + " chương — " + percent + "%");
            holder.progressBar.setMax(100);
            holder.progressBar.setProgress(percent);
        } else {
            holder.txtStatus.setText("Chưa đọc");
            holder.progressBar.setProgress(0);
        }

        // Ảnh bìa
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(book.getCoverUrl())
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .into(holder.imgCover);
        }

        // Nút xóa khỏi danh sách đang đọc
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteBook(book, holder.getAdapterPosition());
            }
        });

        // Click vào item → mở đọc sách
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle, txtAuthor, txtStatus;
        ProgressBar progressBar;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgBookCover);
            txtTitle = itemView.findViewById(R.id.txtBookTitle);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
            txtStatus = itemView.findViewById(R.id.txtReadingStatus);
            progressBar = itemView.findViewById(R.id.progressBarReading);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
