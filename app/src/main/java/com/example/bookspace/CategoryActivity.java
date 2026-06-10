package com.example.bookspace;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.databinding.ActivityCategoryBinding;
import com.example.bookspace.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity implements OnBookClickListener {

    private ActivityCategoryBinding binding;
    private BookAdapter adapter;
    private final List<Book> categoryBooks = new ArrayList<>();
    private BookRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        repo = new BookRepository(this);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý Insets hệ thống
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
        binding.txtCategoryTitle.setText(categoryName != null ? categoryName : "Danh mục");

        binding.btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadCategoryData(categoryName);
    }

    private void setupRecyclerView() {
        binding.recyclerCategoryBooks.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BookAdapter(categoryBooks, this);
        binding.recyclerCategoryBooks.setAdapter(adapter);
    }

    private void loadCategoryData(String category) {
        categoryBooks.clear();
        
        List<BookEntity> entities;
        // Nếu không có category hoặc chọn "Tất cả", lấy toàn bộ sách
        if (category == null || category.equalsIgnoreCase("Tất cả") || category.equalsIgnoreCase(getString(R.string.cat_all))) {
            entities = repo.getAllBooks();
        } else {
            // Lọc sách theo category (chuyển sang IN HOA để khớp với dữ liệu DB)
            entities = repo.getByCategory(category.toUpperCase());
        }

        for (BookEntity entity : entities) {
            categoryBooks.add(Book.fromEntity(entity));
        }

        adapter.updateData(categoryBooks);
    }

    @Override
    public void onBookClick(Book book) {
        // Sử dụng BottomSheet dùng chung để hiển thị chi tiết sách
        BookDetailBottomSheet.show(this, book);
    }
}
