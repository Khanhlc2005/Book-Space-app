package com.example.bookspace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TocAdapter extends RecyclerView.Adapter<TocAdapter.TocViewHolder> {

    private final List<String> chapterNames;
    private int currentChapter = 1;
    private OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(int position);
    }

    public TocAdapter(List<String> chapterNames) {
        this.chapterNames = chapterNames;
    }

    public void setListener(OnChapterClickListener l) {
        this.listener = l;
    }

    public void setCurrentChapter(int chapter) {
        this.currentChapter = chapter;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_toc_chapter, parent, false);
        return new TocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TocViewHolder holder, int position) {
        Context ctx = holder.itemView.getContext();
        int chapterNumber = position + 1;
        boolean isActive = (chapterNumber == currentChapter);

        holder.tvChapterName.setText(chapterNames.get(position));

        int primaryColor = ContextCompat.getColor(ctx, R.color.primary);
        int surfaceColor = ContextCompat.getColor(ctx, R.color.on_surface);
        int mutedColor = ContextCompat.getColor(ctx, R.color.on_surface_variant);

        if (isActive) {
            holder.tvChapterNumber.setTextColor(primaryColor);
            holder.tvChapterNumber.setTextSize(18);
            holder.tvChapterName.setTextColor(primaryColor);
            holder.tvChapterName.setTextSize(16);
            holder.tocActiveDot.setVisibility(View.VISIBLE);
        } else {
            holder.tvChapterNumber.setTextColor(mutedColor);
            holder.tvChapterNumber.setTextSize(15);
            holder.tvChapterName.setTextColor(surfaceColor);
            holder.tvChapterName.setTextSize(15);
            holder.tocActiveDot.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChapterClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapterNames.size();
    }

    static class TocViewHolder extends RecyclerView.ViewHolder {
        TextView tvChapterNumber;
        TextView tvChapterName;
        View tocActiveDot;

        TocViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterNumber = itemView.findViewById(R.id.tv_chapter_number);
            tvChapterName = itemView.findViewById(R.id.tv_chapter_name);
            tocActiveDot = itemView.findViewById(R.id.toc_active_dot);
        }
    }
}
