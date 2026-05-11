package com.example.bookspace;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.repository.BookRepository;
import com.example.bookspace.databinding.ActivityCategoryBinding;
import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private ActivityCategoryBinding binding;
    private BookAdapter adapter;
    private List<Book> categoryBooks = new ArrayList<>();
    private BookRepository repo; // Khai báo BookRepository để lấy dữ liệu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        repo = new BookRepository(this); // Khởi tạo BookRepository
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý Insets để không bị đè bởi thanh hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainCategory, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBar.setPadding(
                    binding.topBar.getPaddingLeft(),
                    insets.top + 16,
                    binding.topBar.getPaddingRight(),
                    binding.topBar.getPaddingBottom()
            );
            binding.recyclerCategoryBooks.setPadding(
                    binding.recyclerCategoryBooks.getPaddingLeft(),
                    binding.recyclerCategoryBooks.getPaddingTop(),
                    binding.recyclerCategoryBooks.getPaddingRight(),
                    insets.bottom + 16
            );
            return WindowInsetsCompat.CONSUMED;
        });

        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        binding.txtCategoryTitle.setText(categoryName);

        binding.btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadCategoryData(categoryName);
    }

    private void setupRecyclerView() {
        // Grid 2 cột giống activity_main.xml
        binding.recyclerCategoryBooks.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BookAdapter(categoryBooks, this);
        binding.recyclerCategoryBooks.setAdapter(adapter);
    }

    private void loadCategoryData(String category) {
        categoryBooks.clear();
        if (category == null) return;

        // Sửa: Gọi database để lấy sách theo danh mục (thay thế mock data)
        List<BookEntity> entities = repo.getByCategory(category.toUpperCase());
        for (BookEntity entity : entities) {
            Book book = new Book(entity.coverUrl, entity.title, entity.author, entity.pages, entity.description, entity.category);
            book.setId(entity.id);
            categoryBooks.add(book);
        }

        adapter.updateData(categoryBooks);
    }

    @Override
    public void onBookClick(Book book) {
        BookDetailBottomSheet.show(this, book);
    }

    private void showBookDetailBottomSheet(Book book) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_book_detail, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Map views
        TextView txtTitle = bottomSheetView.findViewById(R.id.txtDetailTitle);
        TextView txtAuthor = bottomSheetView.findViewById(R.id.txtDetailAuthor);
        TextView txtPages = bottomSheetView.findViewById(R.id.txtDetailPages);
        TextView txtSummary = bottomSheetView.findViewById(R.id.txtDetailSummary);
        ImageView imgCover = bottomSheetView.findViewById(R.id.imgDetailCover);

        // Set data
        if (txtTitle != null) txtTitle.setText(book.getTitle());
        if (txtAuthor != null) txtAuthor.setText("Tác giả: " + book.getAuthor());
        if (txtPages != null) txtPages.setText(String.valueOf(book.getPages()));
        
        if (txtSummary != null) {
            if (book.getDescription() != null && !book.getDescription().isEmpty()) {
                txtSummary.setText(book.getDescription());
            } else {
                txtSummary.setText("Chưa có tóm tắt cho cuốn sách này.");
            }
        }

        if (imgCover != null && book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(this)
                 .load(book.getCoverUrl())
                 .transform(new CenterCrop(), new RoundedCorners(24))
                 .into(imgCover);
        }

        bottomSheetDialog.setOnShowListener(dialogInterface -> {
            com.google.android.material.bottomsheet.BottomSheetDialog dialog = (com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            }
        });

        bottomSheetDialog.show();
    }
}
