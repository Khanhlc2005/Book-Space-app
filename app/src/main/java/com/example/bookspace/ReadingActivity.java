package com.example.bookspace;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.ReadingProgressDao;
import com.example.bookspace.database.entity.ReadingProgressEntity;
import com.example.bookspace.databinding.ActivityReadingBinding;

import java.util.ArrayList;
import java.util.List;

public class ReadingActivity extends AppCompatActivity {

    private ActivityReadingBinding binding;
    private AppDatabase db;
    private ReadingProgressDao progressDao;

    // Trạng thái đọc
    private int currentChapter = 1;
    private int totalChapters = 10;
    private int pagesPerChapter = 10;
    private int bookId = 1;
    private String userId = "default_user";

    // Cỡ chữ
    private final int[] fontSizes = {16, 17, 18, 19, 20, 21, 22};
    private int fontSizeIndex = 3; // mặc định 19sp

    // Danh sách chương
    private List<String> chapterNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo database
        db = AppDatabase.getInstance(this);
        progressDao = db.readingProgressDao();

        // Tải dữ liệu chương
        initChapterData();

        // Tải tiến độ đã lưu
        loadReadingProgress();

        // Cài đặt các thành phần
        setupTopBar();
        setupBottomBar();
        setupSettingsPanel();
        setupTextSizeButtons();
        setupFontButtons();
        setupThemeButtons();
        setupScrollListener();

        // Cập nhật giao diện
        updateDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveReadingProgress();
    }

    // Khởi tạo danh sách chương
    private void initChapterData() {
        chapterNames = new ArrayList<>();
        chapterNames.add("1: Má");
        chapterNames.add("2: Bà");
        chapterNames.add("3: Cha");
        chapterNames.add("4: Người lạ");
        chapterNames.add("5: Cô hàng xóm");
        chapterNames.add("6: Ông già");
        chapterNames.add("7: Hồi ức");
        chapterNames.add("8: Bóng đêm");
        chapterNames.add("9: Ánh sáng");
        chapterNames.add("10: Tái ngộ");
    }

    // Tải tiến độ đã lưu
    private void loadReadingProgress() {
        ReadingProgressEntity progress = progressDao.getProgress(userId, bookId);
        if (progress != null) {
            currentChapter = progress.currentPage + 1;
            if (currentChapter > totalChapters) currentChapter = totalChapters;
        }
    }

    // Lưu tiến độ
    private void saveReadingProgress() {
        ReadingProgressEntity progress = new ReadingProgressEntity();
        progress.userId = userId;
        progress.bookId = bookId;
        progress.currentPage = currentChapter - 1;
        progress.totalPages = totalChapters;
        progress.lastReadAt = System.currentTimeMillis();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            progressDao.updateProgress(progress);
        });
    }

    // Cập nhật hiển thị
    private void updateDisplay() {
        // Cập nhật nhãn chương
        String chapterName = chapterNames.size() > currentChapter - 1
                ? chapterNames.get(currentChapter - 1)
                : "Chương " + currentChapter;
        binding.tvChapterLabel.setText("CHƯƠNG " + currentChapter);
        binding.tvBookTitle.setText(chapterName);

        // Cập nhật số trang
        binding.tvPageNumber.setText("Trang " + currentChapter + " / " + totalChapters);

        // Cập nhật nội dung
        updateContent();
    }

    // Cập nhật nội dung đọc
    private void updateContent() {
        String[] paragraphs = {
                "We are but stewards of these echoes, the Head Librarian had once told him. Elias hadn't understood it then. He was young, obsessed with the digital infinite, the way a billion books could be distilled into a single shard of glass. But as the years turned into a slow, rhythmic dance of cataloging and preservation, he realized that the infinite was often a mask for the empty.",

                "In the heart of the library, the air smelled of almond and old dust—a scent he had come to associate with truth. There were no search bars here. No algorithms to suggest what he might like next. There was only the weight of the book in his hands and the silence that demanded he listen.",

                "He turned the page. The sound was like a sharp intake of breath in the quiet room. On page eighty-four, he found it. A marginal note, written in a hand so fine it looked like lace. It wasn't a correction or a citation. It was a confession.",

                "The curator leaned closer, the Light Teal glow of his modern interface momentarily forgotten on the desk behind him. He was no longer a man of the modern world; he was a traveler in someone else's memory.",

                "The sun began to dip below the horizon of the city outside, casting long, bruised shadows through the high windows of the archive. Elias did not reach for the light switch. He let the darkness bloom, knowing that some words were only meant to be read in the twilight."
        };

        // Lặp lại nội dung theo chương
        int index = (currentChapter - 1) % paragraphs.length;
        binding.tvParagraph1.setText(paragraphs[index]);
        binding.tvParagraph2.setText(paragraphs[(index + 1) % paragraphs.length]);
        binding.tvParagraph3.setText(paragraphs[(index + 2) % paragraphs.length]);
        binding.tvParagraph4.setText(paragraphs[(index + 3) % paragraphs.length]);
        binding.tvParagraph5.setText(paragraphs[(index + 4) % paragraphs.length]);
    }

    // Lắng nghe scroll để cập nhật %
    private void setupScrollListener() {
        binding.readingScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            // Lấy vị trí scroll hiện tại
            View scrollChild = binding.readingScrollView.getChildAt(0);
            if (scrollChild == null) return;

            int scrollViewHeight = binding.readingScrollView.getHeight();
            int totalScrollHeight = scrollChild.getHeight();

            // Tính % dựa trên scroll
            int scrollY = binding.readingScrollView.getScrollY();
            int maxScroll = totalScrollHeight - scrollViewHeight;

            if (maxScroll <= 0) {
                // Không cuộn được (nội dung ngắn)
                binding.tvProgressPercent.setText("100%");
                return;
            }

            // Tính %: scroll đầu = 1%, cuối = 100%
            int percent = Math.round((scrollY * 100f) / maxScroll);
            percent = Math.max(1, Math.min(100, percent)); // 1% - 100%

            binding.tvProgressPercent.setText(percent + "%");
        });
    }

    // ── Thanh trên ──
    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnMenu.setOnClickListener(v -> showTableOfContents());

        binding.btnSettings.setOnClickListener(v -> {
            boolean isVisible = binding.settingsPanel.getVisibility() == View.VISIBLE;
            binding.settingsPanel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });
    }

    // ── Thanh chuyển trang ──
    private void setupBottomBar() {
        binding.btnPrevChapter.setOnClickListener(v -> goToPrevPage());
        binding.btnNextChapter.setOnClickListener(v -> goToNextPage());
    }

    // Chuyển trang trước
    private void goToPrevPage() {
        if (currentChapter <= 1) {
            Toast.makeText(this, "Đang ở trang đầu tiên", Toast.LENGTH_SHORT).show();
            return;
        }
        currentChapter--;
        updateDisplay();
        saveReadingProgress();

        // Cuộn lên đầu
        binding.readingScrollView.scrollTo(0, 0);
    }

    // Chuyển trang tiếp
    private void goToNextPage() {
        if (currentChapter >= totalChapters) {
            Toast.makeText(this, "Đang ở trang cuối cùng", Toast.LENGTH_SHORT).show();
            return;
        }
        currentChapter++;
        updateDisplay();
        saveReadingProgress();

        // Cuộn lên đầu
        binding.readingScrollView.scrollTo(0, 0);
    }

    // Hiện mục lục
    private void showTableOfContents() {
        String[] chapters = new String[chapterNames.size()];
        chapterNames.toArray(chapters);

        new AlertDialog.Builder(this)
                .setTitle("Mục lục")
                .setItems(chapters, (dialog, which) -> {
                    currentChapter = which + 1;
                    updateDisplay();
                    saveReadingProgress();
                    binding.readingScrollView.scrollTo(0, 0);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    // ── Panel cài đặt ──
    private void setupSettingsPanel() {
        binding.sliderBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = progress / 100f;
                    getWindow().setAttributes(params);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar s) {}
            @Override
            public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    // ── Nút cỡ chữ ──
    private void setupTextSizeButtons() {
        binding.btnTextMinus.setOnClickListener(v -> changeFontSize(-1));
        binding.btnTextPlus.setOnClickListener(v -> changeFontSize(1));
    }

    private void changeFontSize(int direction) {
        fontSizeIndex = Math.max(0, Math.min(fontSizes.length - 1, fontSizeIndex + direction));
        float size = fontSizes[fontSizeIndex];
        int pct = Math.round((size / fontSizes[3]) * 100);
        binding.btnText100.setText(pct + "%");

        binding.tvParagraph1.setTextSize(size);
        binding.tvParagraph2.setTextSize(size);
        binding.tvParagraph3.setTextSize(size);
        binding.tvParagraph4.setTextSize(size);
        binding.tvParagraph5.setTextSize(size);
        binding.tvPullQuote.setTextSize(size);
    }

    // ── Nút phông chữ ──
    private void setupFontButtons() {
        binding.btnFontLiterata.setOnClickListener(v -> {
            binding.btnFontLiterata.setBackgroundResource(R.drawable.reading_btn_bg);
            binding.btnFontInter.setBackground(null);
        });

        binding.btnFontInter.setOnClickListener(v -> {
            binding.btnFontInter.setBackgroundResource(R.drawable.reading_btn_bg);
            binding.btnFontLiterata.setBackground(null);
        });
    }

    // ── Nút chủ đề ──
    private void setupThemeButtons() {
        binding.btnThemeLight.setOnClickListener(v -> applyTheme(
                R.color.reading_bg, R.color.reading_text,
                R.color.reading_accent, R.color.reading_text_muted));

        binding.btnThemeSepia.setOnClickListener(v -> applyTheme(
                R.color.reading_bg_sepia, R.color.reading_text_sepia,
                R.color.reading_accent_sepia, R.color.reading_text_muted_sepia));

        binding.btnThemeDark.setOnClickListener(v -> applyTheme(
                R.color.reading_bg_dark, R.color.reading_text_dark,
                R.color.reading_accent_dark, R.color.reading_text_muted_dark));
    }

    private void applyTheme(int bgColor, int textColor, int accentColor, int mutedColor) {
        int bg = ContextCompat.getColor(this, bgColor);
        int text = ContextCompat.getColor(this, textColor);
        int accent = ContextCompat.getColor(this, accentColor);
        int muted = ContextCompat.getColor(this, mutedColor);

        binding.readingRoot.setBackgroundColor(bg);
        binding.topBar.setBackgroundColor(bg);
        binding.bottomStatusBar.setBackgroundColor(bg);

        binding.tvParagraph1.setTextColor(text);
        binding.tvParagraph2.setTextColor(text);
        binding.tvParagraph3.setTextColor(text);
        binding.tvParagraph4.setTextColor(text);
        binding.tvParagraph5.setTextColor(text);
        binding.tvPullQuote.setTextColor(muted);

        binding.tvBookTitle.setTextColor(accent);
        binding.tvChapterLabel.setTextColor(muted);
        binding.tvProgressPercent.setTextColor(accent);
        binding.tvPageNumber.setTextColor(muted);
    }
}
