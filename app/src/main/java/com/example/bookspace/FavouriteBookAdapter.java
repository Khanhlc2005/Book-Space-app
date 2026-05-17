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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

/**
 * Adapter cho danh sách sách yêu thích (FavouritesActivity).
 * Hiển thị ảnh bìa, tên sách, tác giả và nút bỏ yêu thích.
 */
public class FavouriteBookAdapter extends RecyclerView.Adapter<FavouriteBookAdapter.ViewHolder> {

    public interface OnFavouriteActionListener {
        void onRemoveFavourite(Book book, int position);
        void onBookClick(Book book);
    }

    private List<Book> list;
    private final OnFavouriteActionListener listener;

    public FavouriteBookAdapter(List<Book> list, OnFavouriteActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<Book> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < list.size()) {
            list.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_fav, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = list.get(position);

        holder.txtTitle.setText(book.getTitle());
        holder.txtAuthor.setText(book.getAuthor());
        holder.txtStatus.setText(book.getPages() + " trang");

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(book.getCoverUrl())
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .into(holder.imgCover);
        }

        // Nút xóa (cùng chức năng bỏ yêu thích)
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFavourite(book, holder.getAdapterPosition());
            }
        });

        // Click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle, txtAuthor, txtStatus;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgBookCover);
            txtTitle = itemView.findViewById(R.id.txtBookTitle);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
            txtStatus = itemView.findViewById(R.id.txtReadingStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
