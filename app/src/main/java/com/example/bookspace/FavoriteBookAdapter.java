package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FavoriteBookAdapter extends RecyclerView.Adapter<FavoriteBookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private OnItemClickListener listener;

    // 1. Interface để lắng nghe sự kiện nút xóa
    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    // Constructor của Adapter
    public FavoriteBookAdapter(List<Book> bookList, OnItemClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    // 2. Nạp file giao diện cho từng dòng (item_book_favorite.xml)
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_favorite, parent, false);
        return new BookViewHolder(view);
    }

    // 3. Đổ dữ liệu vào các View ở vị trí tương ứng
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book currentBook = bookList.get(position);

        // Gắn chữ và ảnh
        holder.tvTitle.setText(currentBook.getTitle());
        holder.tvAuthor.setText(currentBook.getAuthor());
        holder.tvStatus.setText(currentBook.getStatus());
        holder.imgCover.setImageResource(currentBook.getCoverResId());

        // Xử lý sự kiện khi bấm nút thùng rác
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            }
        });
    }

    // 4. Cho biết danh sách có tổng cộng bao nhiêu cuốn sách
    @Override
    public int getItemCount() {
        return bookList.size();
    }

    // 5. Lớp ViewHolder: Ánh xạ các thành phần giao diện của item_book_favorite.xml
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvAuthor, tvStatus;
        ImageButton btnDelete;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}