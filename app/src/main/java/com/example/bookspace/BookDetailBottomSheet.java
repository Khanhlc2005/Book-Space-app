package com.example.bookspace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.ReviewEntity;
import com.example.bookspace.repository.BookRepository;
import com.example.bookspace.repository.FavouriteRepository;
import com.example.bookspace.repository.ReviewRepository;
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
        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

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
        setupReviews(activity, bookId, bottomSheetView);
        setupRelatedBooks(activity, bottomSheetDialog, bookRepository, book, bookId, bottomSheetView);

        if (btnPrimaryAction != null) {
            btnPrimaryAction.setOnClickListener(v -> {
                if (bookRepository.isDownloaded(bookId)) {
                    Intent intent = new Intent(activity, ReadingActivity.class);
                    intent.putExtra("BOOK_ID", bookId);
                    intent.putExtra("BOOK_TITLE", book.getTitle());
                    intent.putExtra("SOURCE_PAGE", R.id.nav_library);
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

    private static void setupReviews(Activity activity, int bookId, View bottomSheetView) {
        ReviewRepository reviewRepo = new ReviewRepository(activity);

        TextView txtAverageRating = bottomSheetView.findViewById(R.id.txtAverageRating);
        TextView txtReviewCount = bottomSheetView.findViewById(R.id.txtReviewCount);
        TextView txtDetailFavorites = bottomSheetView.findViewById(R.id.txtDetailFavorites);
        RatingBar rbInput = bottomSheetView.findViewById(R.id.rbInput);
        EditText etReviewInput = bottomSheetView.findViewById(R.id.etReviewInput);
        View btnSubmitReview = bottomSheetView.findViewById(R.id.btnSubmitReview);
        RecyclerView rvReviews = bottomSheetView.findViewById(R.id.rvReviews);
        TextView txtReviewsEmpty = bottomSheetView.findViewById(R.id.txtReviewsEmpty);

        if (rvReviews != null) {
            rvReviews.setLayoutManager(new LinearLayoutManager(activity));
        }

        // reloadHolder để phá vòng phụ thuộc: callback xoá cần gọi reload, reload cần adapter.
        final Runnable[] reloadHolder = new Runnable[1];
        ReviewAdapter adapter = new ReviewAdapter(new java.util.ArrayList<>(), reviewRepo.getCurrentUserId(), review -> {
            reviewRepo.deleteMyReview(bookId);
            if (rbInput != null) rbInput.setRating(0);
            if (etReviewInput != null) etReviewInput.setText("");
            reloadHolder[0].run();
            Toast.makeText(activity, R.string.review_deleted, Toast.LENGTH_SHORT).show();
        });
        if (rvReviews != null) {
            rvReviews.setAdapter(adapter);
        }

        Runnable reload = () -> {
            List<ReviewEntity> list = reviewRepo.getReviews(bookId);
            adapter.updateData(list);
            String formattedAverage = String.format(Locale.getDefault(), "%.1f", reviewRepo.getAverage(bookId));
            if (txtAverageRating != null) {
                txtAverageRating.setText(formattedAverage);
            }
            if (txtDetailFavorites != null) {
                txtDetailFavorites.setText(formattedAverage);
            }
            if (txtReviewCount != null) {
                txtReviewCount.setText(activity.getString(R.string.book_review_count_format, reviewRepo.getCount(bookId)));
            }
            boolean empty = list.isEmpty();
            if (rvReviews != null) {
                rvReviews.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
            if (txtReviewsEmpty != null) {
                txtReviewsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        };
        reloadHolder[0] = reload;

        // Prefill review cũ của người dùng (nếu có) để cho phép sửa
        ReviewEntity mine = reviewRepo.getMyReview(bookId);
        if (mine != null) {
            if (rbInput != null) rbInput.setRating(mine.rating);
            if (etReviewInput != null) etReviewInput.setText(mine.content);
        }

        if (btnSubmitReview != null) {
            btnSubmitReview.setOnClickListener(v -> {
                int rating = rbInput == null ? 0 : (int) rbInput.getRating();
                String content = etReviewInput == null ? "" : etReviewInput.getText().toString().trim();
                if (rating < 1) {
                    Toast.makeText(activity, R.string.review_need_rating, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (content.isEmpty()) {
                    Toast.makeText(activity, R.string.review_need_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                reviewRepo.submitReview(bookId, rating, content);
                hideKeyboard(activity, etReviewInput);
                reload.run();
                Toast.makeText(activity, R.string.review_submitted, Toast.LENGTH_SHORT).show();
            });
        }

        reload.run();
    }

    private static void hideKeyboard(Activity activity, View view) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
            List<BookEntity> all = bookRepository.getAllBooks();
            relatedBooks = new java.util.ArrayList<>();
            for (BookEntity e : all) {
                if (e.author.equals(book.getAuthor()) && e.id != bookId) {
                    relatedBooks.add(Book.fromEntity(e));
                    if (relatedBooks.size() >= 8) break;
                }
            }
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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
