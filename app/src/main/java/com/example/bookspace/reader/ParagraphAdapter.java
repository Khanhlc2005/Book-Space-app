package com.example.bookspace.reader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookspace.R;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter hiển thị các đoạn văn trong một chương sách.
 * Hỗ trợ 2 loại view:
 * - TYPE_NORMAL (0): Đoạn văn thường
 * - TYPE_QUOTE (1): Trích dẫn nổi bật (pull quote) với thanh dọc accent bên trái
 */
public class ParagraphAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<BookContent.Paragraph> paragraphs = new ArrayList<>();
    private float textSize = 19f; // sp, mặc định
    private int textColor = 0;
    private int mutedColor = 0;
    private int accentColor = 0;
    private boolean colorsInitialized = false;

    public void submitList(List<BookContent.Paragraph> newParagraphs) {
        this.paragraphs = newParagraphs != null ? newParagraphs : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateTextSize(float newSize) {
        this.textSize = newSize;
        notifyDataSetChanged();
    }

    public void updateColors(int textColor, int mutedColor, int accentColor) {
        this.textColor = textColor;
        this.mutedColor = mutedColor;
        this.accentColor = accentColor;
        this.colorsInitialized = true;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return paragraphs.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == BookContent.Paragraph.TYPE_QUOTE) {
            View view = inflater.inflate(R.layout.item_paragraph_quote, parent, false);
            return new QuoteViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_paragraph_normal, parent, false);
            return new NormalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BookContent.Paragraph paragraph = paragraphs.get(position);

        if (holder instanceof NormalViewHolder) {
            NormalViewHolder vh = (NormalViewHolder) holder;
            vh.tvParagraph.setText(paragraph.getText());
            vh.tvParagraph.setTextSize(textSize);
            if (colorsInitialized) {
                vh.tvParagraph.setTextColor(textColor);
            }
        } else if (holder instanceof QuoteViewHolder) {
            QuoteViewHolder vh = (QuoteViewHolder) holder;
            vh.tvQuote.setText(paragraph.getText());
            vh.tvQuote.setTextSize(textSize);
            if (colorsInitialized) {
                vh.tvQuote.setTextColor(mutedColor);
                vh.accentBar.setBackgroundColor(accentColor);
            }
        }
    }

    @Override
    public int getItemCount() {
        return paragraphs.size();
    }

    static class NormalViewHolder extends RecyclerView.ViewHolder {
        TextView tvParagraph;

        NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParagraph = itemView.findViewById(R.id.tv_paragraph);
        }
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuote;
        View accentBar;

        QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuote = itemView.findViewById(R.id.tv_quote);
            accentBar = itemView.findViewById(R.id.accent_bar);
        }
    }
}
