package com.example.bookspace;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.databinding.ActivityChallengeBinding;
import com.example.bookspace.repository.BookRepository;
import com.example.bookspace.repository.ProgressRepository;

import java.util.List;

public class ChallengeActivity extends AppCompatActivity {

    private ActivityChallengeBinding binding;
    private ChallengeRecentAdapter recentAdapter;
    private ProgressRepository progressRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Cài đặt giao diện tối cho Activity này
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(0); // Bỏ sáng icon status bar

        binding = ActivityChallengeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressRepository = new ProgressRepository(this);
        
        setupUI();
        setupBottomNav();
    }

    private void setupUI() {
        // Thiết lập danh sách Previous (sách vừa đọc)
        recentAdapter = new ChallengeRecentAdapter(book -> {
            // Mở lại sách đang đọc
            startActivity(ReadingActivity.createIntent(this, book.id, R.id.nav_challenge));
        });
        
        binding.rvPreviousBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPreviousBooks.setAdapter(recentAdapter);

        // Load dữ liệu sách gần đây
        List<BookEntity> books = progressRepository.getBooksInReadingProgress();
        recentAdapter.setItems(books);

        // Giả lập tiến độ (Trong thực tế sẽ tính toán từ session đọc)
        binding.pbArcGoal.setProgress(65);
        binding.tvReadingTime.setText("3:15");
        
        binding.btnExplore.setOnClickListener(v -> {
            // Quay lại trang chủ để khám phá
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void setupBottomNav() {
        // Cập nhật icon active trên thanh điều hướng dưới
        MainActivity.updateBottomNavIcon(this, R.id.nav_challenge);

        binding.bottomNavChallenge.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        binding.bottomNavChallenge.navReader.setOnClickListener(v -> {
            openLastReadingBook();
        });

        binding.bottomNavChallenge.navLibrary.setOnClickListener(v -> {
            startActivity(new Intent(this, FavouritesActivity.class));
            finish();
        });
    }

    private void openLastReadingBook() {
        int lastBookId = ReadingActivity.getLastBookId(this);
        if (lastBookId > 0) {
            startActivity(ReadingActivity.createIntent(this, lastBookId, R.id.nav_challenge));
            return;
        }
        
        List<BookEntity> booksInProgress = progressRepository.getBooksInReadingProgress();
        if (booksInProgress != null && !booksInProgress.isEmpty()) {
            startActivity(ReadingActivity.createIntent(this, booksInProgress.get(0).id, R.id.nav_challenge));
        } else {
            startActivity(new Intent(this, CurrentlyReadingListActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.updateBottomNavIcon(this, R.id.nav_challenge);
        // Cập nhật lại danh sách nếu có thay đổi
        recentAdapter.setItems(progressRepository.getBooksInReadingProgress());
    }
}
