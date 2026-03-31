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

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_book_rview, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        if (book == null) return;

        holder.tvBookTitle.setText(book.getTitle());
        holder.tvBookAuthor.setText(book.getAuthor());

        // Sử dụng Glide để tải ảnh từ URL
        Glide.with(holder.itemView.getContext())
                .load(book.getImageUrl())
                .placeholder(R.drawable.book_cover_bg) // Ảnh tạm trong lúc load
                .error(R.drawable.book_cover_bg)       // Ảnh hiển thị nếu lỗi
                .transform(new CenterCrop(), new RoundedCorners(24))
                .into(holder.imgBookCover);
    }

    @Override
    public int getItemCount() {
        return (bookList != null) ? bookList.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBookCover;
        TextView tvBookTitle;
        TextView tvBookAuthor;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBookCover = itemView.findViewById(R.id.imgBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
        }
    }
}
