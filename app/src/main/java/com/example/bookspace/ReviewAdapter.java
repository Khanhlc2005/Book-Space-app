package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookspace.database.entity.ReviewEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public interface OnDeleteListener {
        void onDelete(ReviewEntity review);
    }

    private final List<ReviewEntity> reviews;
    private final String currentUserId;
    private final OnDeleteListener deleteListener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ReviewAdapter(List<ReviewEntity> reviews, String currentUserId, OnDeleteListener deleteListener) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<ReviewEntity> newReviews) {
        reviews.clear();
        if (newReviews != null) {
            reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewEntity review = reviews.get(position);

        holder.txtAuthor.setText(review.userId);
        holder.rbStars.setRating(review.rating);
        holder.txtContent.setText(review.content == null ? "" : review.content);
        holder.txtDate.setText(dateFormat.format(new Date(review.createdAt)));

        boolean isMine = currentUserId != null && currentUserId.equals(review.userId);
        if (isMine) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(review);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnDelete.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        final TextView txtAuthor;
        final RatingBar rbStars;
        final TextView txtContent;
        final TextView txtDate;
        final ImageView btnDelete;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAuthor = itemView.findViewById(R.id.txtReviewAuthor);
            rbStars = itemView.findViewById(R.id.rbReviewStars);
            txtContent = itemView.findViewById(R.id.txtReviewContent);
            txtDate = itemView.findViewById(R.id.txtReviewDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteReview);
        }
    }
}
