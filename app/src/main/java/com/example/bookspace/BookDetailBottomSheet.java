package com.example.bookspace;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.bookspace.repository.BookRepository;
import com.example.bookspace.repository.FavouriteRepository;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.Locale;

public final class BookDetailBottomSheet {
    private BookDetailBottomSheet() {
    }

    public static void show(Activity activity, Book book) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        View bottomSheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_book_detail, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        BookRepository bookRepository = new BookRepository(activity);
        FavouriteRepository favouriteRepository = new FavouriteRepository(activity);
        int bookId = bookRepository.saveOrGetBookId(book);

        TextView txtTitle = bottomSheetView.findViewById(R.id.txtDetailTitle);
        TextView txtAuthor = bottomSheetView.findViewById(R.id.txtDetailAuthor);
        TextView txtPages = bottomSheetView.findViewById(R.id.txtDetailPages);
        TextView txtSummary = bottomSheetView.findViewById(R.id.txtDetailSummary);
        ImageView imgCover = bottomSheetView.findViewById(R.id.imgDetailCover);
        CardView btnPrimaryAction = bottomSheetView.findViewById(R.id.btnDownload);
        TextView txtPrimaryAction = bottomSheetView.findViewById(R.id.txtPrimaryAction);
        ImageView imgPrimaryActionIcon = bottomSheetView.findViewById(R.id.imgPrimaryActionIcon);
        TextView txtDownloadStatus = bottomSheetView.findViewById(R.id.txtDownloadStatus);
        CardView btnFavorite = bottomSheetView.findViewById(R.id.btnFavorite);
        ImageView imgFavoriteAction = bottomSheetView.findViewById(R.id.imgFavoriteAction);

        if (txtTitle != null) txtTitle.setText(book.getTitle());
        if (txtAuthor != null) txtAuthor.setText("Tác giả: " + book.getAuthor());
        if (txtPages != null) txtPages.setText(String.valueOf(book.getPages()));

        if (txtSummary != null) {
            String description = book.getDescription();
            txtSummary.setText(description == null || description.isEmpty()
                    ? "Chưa có tóm tắt cho cuốn sách này."
                    : description);
        }

        if (imgCover != null && book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(activity)
                    .load(book.getCoverUrl())
                    .transform(new CenterCrop(), new RoundedCorners(24))
                    .into(imgCover);
        }

        updateDownloadUi(activity, bookRepository, bookId, btnPrimaryAction, txtPrimaryAction, imgPrimaryActionIcon, txtDownloadStatus, false);
        updateFavouriteAction(favouriteRepository.isFavourite(bookId), imgFavoriteAction);
        setupReviews(activity, book, bottomSheetView);
        setupRelatedBooks(activity, bottomSheetDialog, bookRepository, book, bookId, bottomSheetView);

        if (btnPrimaryAction != null) {
            btnPrimaryAction.setOnClickListener(v -> {
                if (bookRepository.isDownloaded(bookId)) {
                    Intent intent = new Intent(activity, ReadingActivity.class);
                    intent.putExtra("BOOK_ID", bookId);
                    intent.putExtra("BOOK_TITLE", book.getTitle());
                    activity.startActivity(intent);
                    bottomSheetDialog.dismiss();
                } else {
                    updateDownloadUi(activity, bookRepository, bookId, btnPrimaryAction, txtPrimaryAction, imgPrimaryActionIcon, txtDownloadStatus, true);
                    btnPrimaryAction.postDelayed(() -> {
                        bookRepository.markDownloaded(bookId);
                        updateDownloadUi(activity, bookRepository, bookId, btnPrimaryAction, txtPrimaryAction, imgPrimaryActionIcon, txtDownloadStatus, false);
                        Toast.makeText(activity, R.string.book_downloaded_success, Toast.LENGTH_SHORT).show();
                    }, 700);
                }
            });
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                boolean isFavourite = favouriteRepository.toggleSync(bookId);
                updateFavouriteAction(isFavourite, imgFavoriteAction);
                Toast.makeText(
                        activity,
                        isFavourite ? R.string.book_favourite_added : R.string.book_favourite_removed,
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        bottomSheetDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });

        bottomSheetDialog.show();
    }

    private static void setupReviews(Activity activity, Book book, View bottomSheetView) {
        TextView txtAverageRating = bottomSheetView.findViewById(R.id.txtAverageRating);
        TextView txtReviewCount = bottomSheetView.findViewById(R.id.txtReviewCount);
        TextView txtDetailFavorites = bottomSheetView.findViewById(R.id.txtDetailFavorites);
        TextView txtReviewOneName = bottomSheetView.findViewById(R.id.txtReviewOneName);
        TextView txtReviewOneRating = bottomSheetView.findViewById(R.id.txtReviewOneRating);
        TextView txtReviewOneBody = bottomSheetView.findViewById(R.id.txtReviewOneBody);
        TextView txtReviewTwoName = bottomSheetView.findViewById(R.id.txtReviewTwoName);
        TextView txtReviewTwoRating = bottomSheetView.findViewById(R.id.txtReviewTwoRating);
        TextView txtReviewTwoBody = bottomSheetView.findViewById(R.id.txtReviewTwoBody);

        long seed = Math.abs((long) (safeText(book.getTitle()) + safeText(book.getAuthor())).hashCode());
        double averageRating = 4.2d + (seed % 7) / 10.0d;
        int reviewCount = 48 + (int) (seed % 560);
        String formattedAverage = String.format(Locale.getDefault(), "%.1f", averageRating);

        if (txtAverageRating != null) {
            txtAverageRating.setText(formattedAverage);
        }
        if (txtReviewCount != null) {
            txtReviewCount.setText(activity.getString(R.string.book_review_count_format, reviewCount));
        }
        if (txtDetailFavorites != null) {
            txtDetailFavorites.setText(formattedAverage);
        }

        String[] reviewers = activity.getResources().getStringArray(R.array.book_reviewers);
        String[] reviewBodies = activity.getResources().getStringArray(R.array.book_review_bodies);

        int firstIndex = (int) (seed % reviewers.length);
        int secondIndex = (firstIndex + 2) % reviewers.length;
        bindReview(
                txtReviewOneName,
                txtReviewOneRating,
                txtReviewOneBody,
                reviewers[firstIndex],
                averageRating,
                reviewBodies[firstIndex]
        );
        bindReview(
                txtReviewTwoName,
                txtReviewTwoRating,
                txtReviewTwoBody,
                reviewers[secondIndex],
                Math.max(4.0d, averageRating - 0.2d),
                reviewBodies[secondIndex]
        );
    }

    private static void setupRelatedBooks(Activity activity,
                                          BottomSheetDialog currentDialog,
                                          BookRepository bookRepository,
                                          Book book,
                                          int bookId,
                                          View bottomSheetView) {
        TextView txtRelatedTitle = bottomSheetView.findViewById(R.id.txtRelatedTitle);
        TextView txtRelatedEmpty = bottomSheetView.findViewById(R.id.txtRelatedEmpty);
        RecyclerView rvRelatedBooks = bottomSheetView.findViewById(R.id.rvRelatedBooks);

        List<Book> relatedBooks = null;
        if (!isBlank(book.getAuthor())) {
            relatedBooks = bookRepository.getBooksByAuthorExcept(book.getAuthor(), bookId, 8);
        }

        if (txtRelatedTitle != null) {
            txtRelatedTitle.setText(R.string.book_related_same_author);
        }

        if (relatedBooks == null || relatedBooks.isEmpty()) {
            if (rvRelatedBooks != null) {
                rvRelatedBooks.setVisibility(View.GONE);
            }
            if (txtRelatedEmpty != null) {
                txtRelatedEmpty.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (txtRelatedEmpty != null) {
            txtRelatedEmpty.setVisibility(View.GONE);
        }
        if (rvRelatedBooks != null) {
            rvRelatedBooks.setVisibility(View.VISIBLE);
            rvRelatedBooks.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
            rvRelatedBooks.setAdapter(new RelatedBookAdapter(relatedBooks, relatedBook -> {
                currentDialog.dismiss();
                show(activity, relatedBook);
            }));
        }
    }

    private static void bindReview(TextView txtName,
                                   TextView txtRating,
                                   TextView txtBody,
                                   String reviewer,
                                   double rating,
                                   String body) {
        if (txtName != null) {
            txtName.setText(reviewer);
        }
        if (txtRating != null) {
            txtRating.setText(String.format(Locale.getDefault(), "%.1f/5", rating));
        }
        if (txtBody != null) {
            txtBody.setText(body);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }

    private static void updateDownloadUi(Activity activity,
                                         BookRepository bookRepository,
                                         int bookId,
                                         CardView btnPrimaryAction,
                                         TextView txtPrimaryAction,
                                         ImageView imgPrimaryActionIcon,
                                         TextView txtDownloadStatus,
                                         boolean isDownloading) {
        boolean isDownloaded = bookRepository.isDownloaded(bookId);
        if (btnPrimaryAction != null) {
            btnPrimaryAction.setEnabled(!isDownloading);
            btnPrimaryAction.setAlpha(isDownloading ? 0.78f : 1f);
        }

        if (txtPrimaryAction != null) {
            if (isDownloading) {
                txtPrimaryAction.setText(R.string.book_action_downloading);
            } else {
                txtPrimaryAction.setText(isDownloaded
                        ? R.string.book_action_read
                        : R.string.book_action_download);
            }
        }
        if (imgPrimaryActionIcon != null) {
            imgPrimaryActionIcon.setImageResource(isDownloaded
                    ? R.drawable.ic_menu_book
                    : R.drawable.ic_download);
        }
        if (txtDownloadStatus != null) {
            if (isDownloading) {
                txtDownloadStatus.setText(R.string.book_download_status_downloading);
                txtDownloadStatus.setBackgroundResource(R.drawable.chip_bg);
                txtDownloadStatus.setTextColor(ContextCompat.getColor(activity, R.color.primary));
            } else if (isDownloaded) {
                txtDownloadStatus.setText(R.string.book_download_status_downloaded);
                txtDownloadStatus.setBackgroundResource(R.drawable.chip_active_bg);
                txtDownloadStatus.setTextColor(ContextCompat.getColor(activity, R.color.on_primary));
            } else {
                txtDownloadStatus.setText(R.string.book_download_status_not_downloaded);
                txtDownloadStatus.setBackgroundResource(R.drawable.chip_bg);
                txtDownloadStatus.setTextColor(ContextCompat.getColor(activity, R.color.on_surface_variant));
            }
        }
    }

    private static void updateFavouriteAction(boolean isFavourite, ImageView imgFavoriteAction) {
        if (imgFavoriteAction != null) {
            imgFavoriteAction.setImageResource(isFavourite
                    ? R.drawable.ic_favorite
                    : R.drawable.ic_favorite_border);
        }
    }
}
