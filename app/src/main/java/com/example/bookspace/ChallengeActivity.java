package com.example.bookspace;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.databinding.ActivityChallengeBinding;
import com.example.bookspace.repository.ProgressRepository;

import java.util.List;
import java.util.Locale;

public class ChallengeActivity extends AppCompatActivity {

    private ActivityChallengeBinding binding;
    private ChallengeRecentAdapter recentAdapter;
    private ProgressRepository progressRepository;
    private static final String PREFS_NAME = "ChallengePrefs";
    private static final String KEY_DAILY_GOAL = "daily_reading_goal";
    private int dailyGoalMinutes = 5; // Mặc định 5 phút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Cài đặt giao diện tối cho Activity này
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(0);

        binding = ActivityChallengeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressRepository = new ProgressRepository(this);
        loadGoal();
        
        setupUI();
        setupBottomNav();
    }

    private void loadGoal() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dailyGoalMinutes = prefs.getInt(KEY_DAILY_GOAL, 5);
    }

    private void saveGoal(int minutes) {
        dailyGoalMinutes = minutes;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_DAILY_GOAL, minutes).apply();
        updateGoalUI();
    }

    private void setupUI() {
        recentAdapter = new ChallengeRecentAdapter(book -> {
            startActivity(ReadingActivity.createIntent(this, book.id, R.id.nav_challenge));
        });
        
        binding.rvPreviousBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPreviousBooks.setAdapter(recentAdapter);

        List<BookEntity> books = progressRepository.getBooksInReadingProgress();
        recentAdapter.setItems(books);

        updateGoalUI();
        
        // Cho phép nhấn vào vùng mục tiêu để thay đổi
        binding.goalContainer.setOnClickListener(v -> showSetGoalDialog());

        binding.btnExplore.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void updateGoalUI() {
        // Giả lập thời gian đọc (thực tế sẽ tính từ bảng thống kê)
        int readingSeconds = 195; // 3 phút 15 giây
        int minutes = readingSeconds / 60;
        int seconds = readingSeconds % 60;
        
        binding.tvReadingTime.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
        
        // Cập nhật text hiển thị mục tiêu
        binding.tvGoalStatus.setText(String.format(Locale.getDefault(), "trên mục tiêu %d phút >", dailyGoalMinutes));
        
        // Cập nhật ProgressBar
        int progress = (int) ((readingSeconds / (float) (dailyGoalMinutes * 60)) * 100);
        binding.pbArcGoal.setProgress(Math.min(progress, 100));
    }

    private void showSetGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt mục tiêu đọc sách");
        builder.setMessage("Bạn muốn dành bao nhiêu phút để đọc sách mỗi ngày?");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(dailyGoalMinutes));
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                int newGoal = Integer.parseInt(val);
                if (newGoal > 0) {
                    saveGoal(newGoal);
                    Toast.makeText(this, "Đã cập nhật mục tiêu: " + newGoal + " phút", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setupBottomNav() {
        MainActivity.updateBottomNavIcon(this, R.id.nav_challenge);

        binding.bottomNavChallenge.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        binding.bottomNavChallenge.navReader.setOnClickListener(v -> openLastReadingBook());

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
        recentAdapter.setItems(progressRepository.getBooksInReadingProgress());
    }
}
