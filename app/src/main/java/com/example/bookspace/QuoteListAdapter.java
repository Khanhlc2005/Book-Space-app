package com.example.bookspace;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookspace.databinding.ItemReadingQuoteBinding;
import com.example.bookspace.reader.ReaderQuote;

import java.util.ArrayList;
import java.util.List;

public class QuoteListAdapter extends RecyclerView.Adapter<QuoteListAdapter.QuoteViewHolder> {

    private final List<ReaderQuote> quotes = new ArrayList<>();
    private OnQuoteActionListener listener;

    public interface OnQuoteActionListener {
        void onCreateQuoteCard(ReaderQuote quote);
    }

    public QuoteListAdapter(List<ReaderQuote> quoteList) {
        submitList(quoteList);
    }

    public void setListener(OnQuoteActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ReaderQuote> quoteList) {
        quotes.clear();
        if (quoteList != null) {
            quotes.addAll(quoteList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReadingQuoteBinding binding = ItemReadingQuoteBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new QuoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        ReaderQuote quote = quotes.get(position);
        holder.bind(quote, listener);
    }

    @Override
    public int getItemCount() {
        return quotes.size();
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        private final ItemReadingQuoteBinding binding;

        QuoteViewHolder(ItemReadingQuoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ReaderQuote quote, OnQuoteActionListener listener) {
            binding.tvQuoteText.setText(safeText(quote.getText()));
            binding.tvQuoteMeta.setText(buildMetaText(quote));

            binding.getRoot().setOnClickListener(v -> notifyCreateCard(listener, quote));
            binding.btnCreateQuoteCard.setOnClickListener(v -> notifyCreateCard(listener, quote));
        }

        private void notifyCreateCard(OnQuoteActionListener listener, ReaderQuote quote) {
            if (listener != null) {
                listener.onCreateQuoteCard(quote);
            }
        }

        private String buildMetaText(ReaderQuote quote) {
            String chapterName = safeText(quote.getChapterName());
            String pageText = itemView.getContext().getString(
                    R.string.reader_quote_page_format,
                    quote.getPageNumber()
            );

            if (TextUtils.isEmpty(chapterName)) {
                return pageText;
            }
            return chapterName + " - " + pageText;
        }

        private String safeText(String value) {
            return value == null ? "" : value.trim();
        }
    }
}
