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

public class CategoryActivity extends AppCompatActivity implements OnBookClickListener {

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
}
