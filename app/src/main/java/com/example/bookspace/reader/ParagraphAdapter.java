package com.example.bookspace.reader;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookspace.R;
import com.example.bookspace.database.entity.Highlight;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter hiển thị các đoạn văn trong một chương sách.
 * Hỗ trợ 2 loại view:
 * - TYPE_NORMAL (0): Đoạn văn thường
 * - TYPE_QUOTE (1): Trích dẫn nổi bật (pull quote) với thanh dọc accent bên trái
 * Tích hợp tính năng bôi đen chọn văn bản và đánh dấu (highlight) màu vàng.
 */
public class ParagraphAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ACTION_HIGHLIGHT_ID = 999;

    private List<BookContent.Paragraph> paragraphs = new ArrayList<>();
    private List<Highlight> highlights = new ArrayList<>();
    private OnTextSelectedListener onTextSelectedListener;
    private float textSize = 19f; // sp, mặc định
    private int textColor = 0;
    private int mutedColor = 0;
    private int accentColor = 0;
    private boolean colorsInitialized = false;

    public interface OnTextSelectedListener {
        void onHighlightCreated(BookContent.Paragraph paragraph, String selectedText, int selectionStart, int selectionEnd);
    }

    public void submitList(List<BookContent.Paragraph> newParagraphs) {
        this.paragraphs = newParagraphs != null ? newParagraphs : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setHighlights(List<Highlight> highlights) {
        this.highlights = highlights != null ? highlights : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnTextSelectedListener(OnTextSelectedListener listener) {
        this.onTextSelectedListener = listener;
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
            vh.tvParagraph.setTextSize(textSize);
            if (colorsInitialized) {
                vh.tvParagraph.setTextColor(textColor);
            }
            // Áp dụng bôi màu vàng các trích dẫn đã lưu
            bindParagraphText(vh.tvParagraph, paragraph);
            // Cấu hình tính năng bôi đen văn bản và hiển thị menu Đánh dấu
            setupTextSelection(vh.tvParagraph, paragraph);

        } else if (holder instanceof QuoteViewHolder) {
            QuoteViewHolder vh = (QuoteViewHolder) holder;
            vh.tvQuote.setTextSize(textSize);
            if (colorsInitialized) {
                vh.tvQuote.setTextColor(mutedColor);
                vh.accentBar.setBackgroundColor(accentColor);
            }
            // Áp dụng bôi màu vàng các trích dẫn đã lưu
            bindParagraphText(vh.tvQuote, paragraph);
            // Cấu hình tính năng bôi đen văn bản và hiển thị menu Đánh dấu
            setupTextSelection(vh.tvQuote, paragraph);
        }
    }

    @Override
    public int getItemCount() {
        return paragraphs.size();
    }

    /**
     * Gán văn bản kèm theo các thẻ highlight (đánh dấu màu vàng) tương ứng.
     */
    private void bindParagraphText(TextView textView, BookContent.Paragraph paragraph) {
        String originalText = paragraph.getText();
        SpannableString spannable = new SpannableString(originalText);

        int pStart = paragraph.getCharacterOffsetInChapter();
        int pEnd = pStart + originalText.length();

        for (Highlight h : highlights) {
            if (h == null || h.highlightedText == null || h.highlightedText.trim().isEmpty()) {
                continue;
            }
            // Kiểm tra xem đoạn highlight này có cùng chương không
            if (h.chapterIndex == paragraph.getChapterIndex()) {
                int hStart = h.characterOffsetStart;
                int hEnd = hStart + h.highlightedText.length();

                // Kiểm tra sự giao nhau giữa vùng của paragraph [pStart, pEnd] và highlight [hStart, hEnd]
                int overlapStart = Math.max(pStart, hStart);
                int overlapEnd = Math.min(pEnd, hEnd);

                if (overlapStart < overlapEnd) {
                    // Nếu giao nhau, chuyển đổi offset sang hệ toạ độ local của paragraph
                    int localStart = overlapStart - pStart;
                    int localEnd = overlapEnd - pStart;

                    // Đảm bảo không vượt quá độ dài chuỗi gốc
                    localStart = Math.max(0, Math.min(localStart, originalText.length()));
                    localEnd = Math.max(0, Math.min(localEnd, originalText.length()));

                    // Đổi màu nền sang màu vàng (Highlight)
                    spannable.setSpan(
                        new BackgroundColorSpan(android.graphics.Color.YELLOW),
                        localStart,
                        localEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }
        }
        textView.setText(spannable);
    }

    /**
     * Cấu hình Custom ActionMode để thêm nút "Đánh dấu" vào menu bôi đen văn bản của TextView.
     */
    private void setupTextSelection(TextView textView, BookContent.Paragraph paragraph) {
        textView.setTextIsSelectable(true);
        textView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Thêm tuỳ chọn "Đánh dấu" vào menu nổi
                menu.add(0, ACTION_HIGHLIGHT_ID, 0,
                        textView.getContext().getString(R.string.reader_action_highlight));
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == ACTION_HIGHLIGHT_ID) {
                    int selectionStart = textView.getSelectionStart();
                    int selectionEnd = textView.getSelectionEnd();
                    int selStart = Math.min(selectionStart, selectionEnd);
                    int selEnd = Math.max(selectionStart, selectionEnd);
                    CharSequence text = textView.getText();
                    String selectedText = "";
                    if (selStart >= 0 && selEnd > selStart && selEnd <= text.length()) {
                        selectedText = text.subSequence(selStart, selEnd).toString();
                    }
                    if (onTextSelectedListener != null) {
                        onTextSelectedListener.onHighlightCreated(
                            paragraph,
                            selectedText,
                            selStart,
                            selEnd
                        );
                    }
                    mode.finish(); // Đóng menu bôi đen
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
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
