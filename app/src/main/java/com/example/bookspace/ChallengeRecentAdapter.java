package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.databinding.ListItemRecentChallengeBinding;
import java.util.ArrayList;
import java.util.List;

public class ChallengeRecentAdapter extends RecyclerView.Adapter<ChallengeRecentAdapter.ViewHolder> {
    private List<BookEntity> items = new ArrayList<>();
    private final OnBookEntityClickListener listener;

    public interface OnBookEntityClickListener {
        void onBookClick(BookEntity book);
    }

    public ChallengeRecentAdapter(OnBookEntityClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<BookEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRecentChallengeBinding binding = ListItemRecentChallengeBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookEntity item = items.get(position);
        holder.binding.tvBookTitle.setText(item.title);
        holder.binding.tvBookAuthor.setText(item.author);
        
        // Demo tiến độ 9% như trong ảnh hoặc lấy từ database nếu có trường đó
        holder.binding.tvProgress.setText("Book • 9%");

        Glide.with(holder.itemView.getContext())
            .load(item.coverUrl)
            .into(holder.binding.imgBookCover);

        holder.itemView.setOnClickListener(v -> listener.onBookClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ListItemRecentChallengeBinding binding;
        ViewHolder(ListItemRecentChallengeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
