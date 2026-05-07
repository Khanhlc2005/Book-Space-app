package com.example.bookspace;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.bookspace.repository.BookRepository;
import com.example.bookspace.repository.FavouriteRepository;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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

        updatePrimaryAction(activity, bookRepository, bookId, txtPrimaryAction, imgPrimaryActionIcon);
        updateFavouriteAction(favouriteRepository.isFavourite(bookId), imgFavoriteAction);

        if (btnPrimaryAction != null) {
            btnPrimaryAction.setOnClickListener(v -> {
                if (bookRepository.isDownloaded(bookId)) {
                    Intent intent = new Intent(activity, ReadingActivity.class);
                    intent.putExtra("BOOK_ID", bookId);
                    intent.putExtra("BOOK_TITLE", book.getTitle());
                    activity.startActivity(intent);
                    bottomSheetDialog.dismiss();
                } else {
                    bookRepository.markDownloaded(bookId);
                    updatePrimaryAction(activity, bookRepository, bookId, txtPrimaryAction, imgPrimaryActionIcon);
                    Toast.makeText(activity, R.string.book_downloaded_success, Toast.LENGTH_SHORT).show();
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

    private static void updatePrimaryAction(Activity activity,
                                            BookRepository bookRepository,
                                            int bookId,
                                            TextView txtPrimaryAction,
                                            ImageView imgPrimaryActionIcon) {
        boolean isDownloaded = bookRepository.isDownloaded(bookId);
        if (txtPrimaryAction != null) {
            txtPrimaryAction.setText(isDownloaded
                    ? R.string.book_action_read
                    : R.string.book_action_download);
        }
        if (imgPrimaryActionIcon != null) {
            imgPrimaryActionIcon.setImageResource(isDownloaded
                    ? R.drawable.ic_menu_book
                    : R.drawable.ic_download);
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
