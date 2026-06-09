package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookspace.database.entity.Highlight;

import java.util.ArrayList;
import java.util.List;

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.ViewHolder> {

    private List<Highlight> highlights = new ArrayList<>();
    private final OnHighlightClickListener listener;

    public interface OnHighlightClickListener {
        void onHighlightClick(Highlight highlight);
        void onDeleteClick(Highlight highlight, int position);
    }

    public HighlightAdapter(List<Highlight> highlights, OnHighlightClickListener listener) {
        if (highlights != null) {
            this.highlights = new ArrayList<>(highlights);
        }
        this.listener = listener;
    }

    public void updateData(List<Highlight> newHighlights) {
        this.highlights = newHighlights != null ? new ArrayList<>(newHighlights) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < highlights.size()) {
            highlights.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_highlight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Highlight highlight = highlights.get(position);
        holder.txtText.setText(highlight.highlightedText);
        holder.txtInfo.setText(highlight.chapterName);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHighlightClick(highlight);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(highlight, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return highlights.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtText;
        final TextView txtInfo;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtText = itemView.findViewById(R.id.txtHighlightText);
            txtInfo = itemView.findViewById(R.id.txtHighlightInfo);
            btnDelete = itemView.findViewById(R.id.btnDeleteHighlight);
        }
    }
}
