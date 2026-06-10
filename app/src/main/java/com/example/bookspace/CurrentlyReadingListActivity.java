package com.example.bookspace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.ReadingProgressEntity;
import com.example.bookspace.databinding.ActivityReadingBooklistBinding;
import com.example.bookspace.repository.ProgressRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurrentlyReadingListActivity extends AppCompatActivity implements ReadingListAdapter.OnReadingListActionListener {

    private ActivityReadingBooklistBinding binding;
    private ReadingListAdapter adapter;
    private ProgressRepository progressRepository;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadingBooklistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressRepository = new ProgressRepository(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBar.setPadding(
                    binding.topBar.getPaddingLeft(),
                    binding.topBar.getPaddingTop() + insets.top,
                    binding.topBar.getPaddingRight(),
                    binding.topBar.getPaddingBottom()
            );
            binding.bottomNav.bottomNavContainer.setPadding(
                    binding.bottomNav.bottomNavContainer.getPaddingLeft(),
                    binding.bottomNav.bottomNavContainer.getPaddingTop(),
                    binding.bottomNav.bottomNavContainer.getPaddingRight(),
                    insets.bottom + 24
            );
            return WindowInsetsCompat.CONSUMED;
        });

        setupNavigation();
        setupEmptyState();

        binding.rvReading.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReadingListAdapter(new ArrayList<>(), new ArrayList<>(), this);
        binding.rvReading.setAdapter(adapter);

        ImageButton btnReminder = binding.btnReminder;
        if (btnReminder != null) {
            btnReminder.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
                    } else {
                        openReminderActivity();
                    }
                } else {
                    openReminderActivity();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReadingList();
        MainActivity.updateBottomNavIcon(this, R.id.nav_reader);
    }

    private void setupEmptyState() {
        View emptyState = findViewById(R.id.emptyStateReading);
        if (emptyState != null) {
            android.widget.ImageView imgEmptyIcon = emptyState.findViewById(R.id.imgEmptyIcon);
            android.widget.TextView txtEmptyMessage = emptyState.findViewById(R.id.txtEmptyMessage);

            if (imgEmptyIcon != null) {
                imgEmptyIcon.setImageResource(R.drawable.ic_auto_stories);
            }
            if (txtEmptyMessage != null) {
                txtEmptyMessage.setText(R.string.no_reading_books);
            }
        }
    }

    private void openReminderActivity() {
        Intent intent = new Intent(this, ReminderActivity.class);
        startActivity(intent);
    }

    private void setupNavigation() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        android.widget.TextView tabFavorite = findViewById(R.id.tabFavorite);
        if (tabFavorite != null) {
            tabFavorite.setOnClickListener(v -> {
                Intent intent = new Intent(this, FavouritesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }

        binding.bottomNav.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.bottomNav.navReader.setOnClickListener(v -> MainActivity.updateBottomNavIcon(this, R.id.nav_reader));

        binding.bottomNav.navLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        updateBottomNavUi(R.id.nav_reader);
    }

    private void updateBottomNavUi(int activeId) {
        int activeColor = ContextCompat.getColor(this, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        binding.bottomNav.iconHome.setColorFilter(activeId == R.id.nav_home ? activeColor : inactiveColor);
        binding.bottomNav.textHome.setTextColor(activeId == R.id.nav_home ? activeColor : inactiveColor);

        binding.bottomNav.iconReader.setColorFilter(activeId == R.id.nav_reader ? activeColor : inactiveColor);
        binding.bottomNav.textReader.setTextColor(activeId == R.id.nav_reader ? activeColor : inactiveColor);

        binding.bottomNav.iconLibrary.setColorFilter(activeId == R.id.nav_library ? activeColor : inactiveColor);
        binding.bottomNav.textLibrary.setTextColor(activeId == R.id.nav_library ? activeColor : inactiveColor);
    }

    private void loadReadingList() {
        List<BookEntity> bookEntities = progressRepository.getBooksInReadingProgress();
        List<Book> books = new ArrayList<>();
        List<ReadingProgressEntity> progressList = new ArrayList<>();

        for (BookEntity entity : bookEntities) {
            books.add(Book.fromEntity(entity));
            ReadingProgressEntity progress = progressRepository.getProgress(entity.id);
            if (progress != null) {
                progressList.add(progress);
            } else {
                ReadingProgressEntity empty = new ReadingProgressEntity();
                empty.currentPage = 0;
                empty.totalPages = 0;
                progressList.add(empty);
            }
        }

        adapter.updateData(books, progressList);

        View emptyState = findViewById(R.id.emptyStateReading);
        if (emptyState != null) {
            if (books.isEmpty()) {
                binding.rvReading.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvReading.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDeleteBook(Book book, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sách '" + book.getTitle() + "' khỏi danh sách không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    progressRepository.deleteProgress(book.getId());
                    com.example.bookspace.repository.BookRepository bookRepo = new com.example.bookspace.repository.BookRepository(this);
                    bookRepo.removeDownloaded(book.getId());
                    adapter.removeItem(position);
                    if (adapter.getItemCount() == 0) loadReadingList();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onBookClick(Book book) {
        Intent intent = new Intent(this, ReadingActivity.class);
        intent.putExtra("BOOK_ID", book.getId());
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openReminderActivity();
            } else {
                Toast.makeText(this, "Cần quyền thông báo để quản lý nhắc nhở", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
