package com.example.bookspace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FeaturedBookAdapter extends RecyclerView.Adapter<FeaturedBookAdapter.FeaturedViewHolder> {
    private List<Book> bookList;
    private OnBookClickListener listener;

    public FeaturedBookAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    /**
     * Khởi tạo và chứa cấu trúc giao diện XML (list_item_featured_book.xml) cho 1 item trang sách nổi bật.
     */
    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_featured_book, parent, false);
        return new FeaturedViewHolder(view);
    }

    /**
     * Map/đẩy dữ liệu từ đối tượng Book (tiêu đề, tác giả, category, ảnh) tương ứng với vị trí trang
     * vào các View trên giao diện, và đổi màu hình nền luân phiên cho từng thẻ.
     */
    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        Book book = bookList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvFeaturedLabel.setText(book.getCategory());
        holder.tvFeaturedTitle.setText(book.getTitle());
        holder.tvFeaturedAuthor.setText(book.getAuthor());

        Glide.with(context)
                .load(book.getImageUrl())
                .centerCrop()
                .into(holder.imgFeaturedCover);

        if (position % 2 == 0) {
            holder.vFeaturedBg.setBackgroundResource(R.drawable.featured_card_primary_bg);
            holder.tvFeaturedTitle.setTextColor(ContextCompat.getColor(context, R.color.on_primary));
        } else {
            holder.vFeaturedBg.setBackgroundResource(R.drawable.featured_card_tertiary_bg);
            holder.tvFeaturedTitle.setTextColor(ContextCompat.getColor(context, R.color.on_tertiary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        View vFeaturedBg;
        ImageView imgFeaturedCover;
        TextView tvFeaturedLabel;
        TextView tvFeaturedTitle;
        TextView tvFeaturedAuthor;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            vFeaturedBg = itemView.findViewById(R.id.vFeaturedBg);
            imgFeaturedCover = itemView.findViewById(R.id.imgFeaturedCover);
            tvFeaturedLabel = itemView.findViewById(R.id.tvFeaturedLabel);
            tvFeaturedTitle = itemView.findViewById(R.id.tvFeaturedTitle);
            tvFeaturedAuthor = itemView.findViewById(R.id.tvFeaturedAuthor);
        }
    }
}
