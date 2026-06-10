package com.example.bookspace;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.ReadingProgressDao;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.database.entity.ReadingProgressEntity;
import com.example.bookspace.database.entity.ReadingSettingsEntity;
import com.example.bookspace.databinding.ActivityReadingBinding;
import com.example.bookspace.reader.BookContent;
import com.example.bookspace.reader.BookTextParser;
import com.example.bookspace.reader.ParagraphAdapter;
import com.example.bookspace.repository.SettingsRepository;

import java.util.ArrayList;
import java.util.List;

public class ReadingActivity extends AppCompatActivity {

    private ActivityReadingBinding binding;
    private AppDatabase db;
    private ReadingProgressDao progressDao;
    private SettingsRepository settingsRepo;
    private ParagraphAdapter paragraphAdapter;
    private TocAdapter tocAdapter;

    // Dữ liệu sách
    private BookContent bookContent;
    private List<String> chapterNames;

    // Trạng thái đọc
    private int currentPage = 1;     // Trang hiện tại (global, tính trên toàn bộ sách)
    private int totalPages = 1;      // Tổng số trang
    private int bookId = 1;
    private String userId = "default_user";
    private String bookTitle = "Sách";
    private int totalChapters = 1;
    private int sourceNavId = R.id.nav_home;

    // Kindle-style pagination: mỗi "trang" chứa một nhóm paragraph vừa màn hình
    private List<List<BookContent.Paragraph>> allPages; // Danh sách tất cả các trang
    private List<Integer> pageToChapterMap;              // Map: page index → chapter index

    private final int[] fontSizes = {16, 17, 18, 19, 20, 21, 22};
    private int fontSizeIndex = 3;

    // Theme hiện tại
    private String currentTheme = "light";
    private String currentFont = "literata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        progressDao = db.readingProgressDao();
        settingsRepo = new SettingsRepository(this);

        // Nhận bookId từ Intent
        bookId = getIntent().getIntExtra("BOOK_ID", 1);
        sourceNavId = getIntent().getIntExtra("SOURCE_NAV_ID", R.id.nav_home);

        // Setup RecyclerView cho nội dung
        paragraphAdapter = new ParagraphAdapter();
        binding.rvContent.setLayoutManager(new LinearLayoutManager(this));
        binding.rvContent.setAdapter(paragraphAdapter);

        // Tải thông tin sách và nội dung
        loadBookInfo();
        loadBookContent();

        // Load cài đặt đọc đã lưu (Phase 4)
        loadSavedSettings();

        // Load tiến độ đọc
        loadReadingProgress();

        setupTopBar();
        setupBottomBar();
        setupSettingsPanel();
        setupTextSizeButtons();
        setupFontButtons();
        setupThemeButtons();
        setupScrollListener();

        updateDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveReadingProgress();
    }

    // ====================================================================
    // LOAD DỮ LIỆU
    // ====================================================================

    private void loadBookInfo() {
        BookEntity bookEntity = db.bookDao().getBookById(bookId);
        if (bookEntity != null) {
            bookTitle = bookEntity.title;
            totalChapters = bookEntity.pages;
        }
    }

    /**
     * Tải nội dung sách từ file .txt trong assets
     * và phân trang theo kiểu Kindle (mỗi trang ~3-4 paragraph)
     */
    private void loadBookContent() {
        BookEntity bookEntity = db.bookDao().getBookById(bookId);

        if (bookEntity != null && bookEntity.bookFilePath != null && !bookEntity.bookFilePath.isEmpty()) {
            // Parse file sách thật
            bookContent = BookTextParser.parse(this, bookEntity.bookFilePath);
        }

        if (bookContent != null && bookContent.getChapterCount() > 0) {
            chapterNames = bookContent.getChapterNames();
            paginateContent();
        } else {
            // Fallback: sách chưa có nội dung file
            chapterNames = new ArrayList<>();
            chapterNames.add("Chưa có nội dung");

            allPages = new ArrayList<>();
            List<BookContent.Paragraph> emptyPage = new ArrayList<>();
            emptyPage.add(new BookContent.Paragraph(
                    "Cuốn sách \"" + bookTitle + "\" chưa có nội dung. Vui lòng thêm file .txt vào thư mục assets/books/.",
                    BookContent.Paragraph.TYPE_NORMAL));
            allPages.add(emptyPage);

            pageToChapterMap = new ArrayList<>();
            pageToChapterMap.add(0);

            totalPages = 1;
        }
    }

    /**
     * Chia toàn bộ nội dung sách thành các trang nhỏ (Kindle-style).
     * Mỗi trang chứa tối đa PARAGRAPHS_PER_PAGE đoạn văn.
     */
    private static final int PARAGRAPHS_PER_PAGE = 3;

    private void paginateContent() {
        allPages = new ArrayList<>();
        pageToChapterMap = new ArrayList<>();

        for (int chapterIdx = 0; chapterIdx < bookContent.getChapterCount(); chapterIdx++) {
            BookContent.Chapter chapter = bookContent.getChapters().get(chapterIdx);
            List<BookContent.Paragraph> paragraphs = chapter.getParagraphs();

            // Chia paragraph của chương thành các trang
            for (int i = 0; i < paragraphs.size(); i += PARAGRAPHS_PER_PAGE) {
                int end = Math.min(i + PARAGRAPHS_PER_PAGE, paragraphs.size());
                List<BookContent.Paragraph> page = new ArrayList<>(paragraphs.subList(i, end));
                allPages.add(page);
                pageToChapterMap.add(chapterIdx);
            }

            // Nếu chương rỗng, tạo 1 trang trống cho chương đó
            if (paragraphs.isEmpty()) {
                List<BookContent.Paragraph> emptyPage = new ArrayList<>();
                emptyPage.add(new BookContent.Paragraph("(Chương trống)", BookContent.Paragraph.TYPE_NORMAL));
                allPages.add(emptyPage);
                pageToChapterMap.add(chapterIdx);
            }
        }

        totalPages = allPages.size();
    }

    // ====================================================================
    // TIẾN ĐỘ ĐỌC
    // ====================================================================

    private void loadReadingProgress() {
        ReadingProgressEntity progress = progressDao.getProgress("default_user", bookId);
        if (progress != null) {
            int savedPage = progress.currentPage + 1; // DB lưu 0-indexed
            if (savedPage > totalPages) {
                currentPage = totalPages;
            } else if (savedPage < 1) {
                currentPage = 1;
            } else {
                currentPage = savedPage;
            }
        } else {
            currentPage = 1;
        }
    }

    private void saveReadingProgress() {
        ReadingProgressEntity progress = new ReadingProgressEntity();
        progress.userId = "default_user";
        progress.bookId = bookId;
        progress.currentPage = currentPage - 1; // Lưu 0-indexed
        progress.totalPages = totalPages;
        progress.lastReadAt = System.currentTimeMillis();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            progressDao.updateProgress(progress);
        });
    }

    // ====================================================================
    // CÀI ĐẶT ĐỌC (Phase 4)
    // ====================================================================

    private void loadSavedSettings() {
        ReadingSettingsEntity settings = settingsRepo.getSettings();
        // Áp dụng cỡ chữ
        fontSizeIndex = findFontIndex(settings.fontSize);
        // Áp dụng theme
        currentTheme = settings.theme;
        applyThemeByName(currentTheme);
        // Áp dụng font
        currentFont = settings.fontFamily;
        applyFontByName(currentFont);
        // Áp dụng độ sáng
        binding.sliderBrightness.setProgress(settings.brightness);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = settings.brightness / 100f;
        getWindow().setAttributes(params);
        // Cập nhật label cỡ chữ
        int pct = Math.round((fontSizes[fontSizeIndex] / (float) fontSizes[3]) * 100);
        binding.btnText100.setText(pct + "%");
    }

    private void saveCurrentSettings() {
        settingsRepo.saveSettings(
                fontSizes[fontSizeIndex],
                currentFont,
                currentTheme,
                binding.sliderBrightness.getProgress()
        );
    }

    private int findFontIndex(int fontSize) {
        for (int i = 0; i < fontSizes.length; i++) {
            if (fontSizes[i] == fontSize) return i;
        }
        return 3; // mặc định
    }

    // ====================================================================
    // HIỂN THỊ
    // ====================================================================

    private void updateDisplay() {
        // Xác định chương hiện tại dựa trên trang
        int chapterIdx = 0;
        if (currentPage >= 1 && currentPage <= pageToChapterMap.size()) {
            chapterIdx = pageToChapterMap.get(currentPage - 1);
        }

        String chapterName = chapterIdx < chapterNames.size()
                ? chapterNames.get(chapterIdx)
                : "Chương " + (chapterIdx + 1);

        binding.tvChapterLabel.setText("CHƯƠNG " + (chapterIdx + 1));
        binding.tvBookTitle.setText(chapterName);
        binding.tvPageNumber.setText("Trang " + currentPage + " / " + totalPages);

        // Hiển thị nội dung trang
        if (currentPage >= 1 && currentPage <= allPages.size()) {
            paragraphAdapter.submitList(allPages.get(currentPage - 1));
        }

        // Cập nhật % tiến độ
        int percent = totalPages > 0 ? Math.round((currentPage * 100f) / totalPages) : 0;
        binding.tvProgressPercent.setText(percent + "%");

        // Cập nhật TOC
        if (tocAdapter != null) {
            tocAdapter.setCurrentChapter(chapterIdx + 1);
        }

        // Cập nhật cỡ chữ cho adapter
        paragraphAdapter.updateTextSize(fontSizes[fontSizeIndex]);
    }

    // ====================================================================
    // ĐIỀU HƯỚNG
    // ====================================================================

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> {
            MainActivity.updateBottomNavIcon(this, sourceNavId);
            finish();
        });
        binding.btnMenu.setOnClickListener(v -> showTableOfContents());

        binding.btnSettings.setOnClickListener(v -> {
            boolean isVisible = binding.settingsPanel.getVisibility() == View.VISIBLE;
            binding.settingsPanel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });
    }

    private void setupBottomBar() {
        binding.btnPrevChapter.setOnClickListener(v -> goToPrevPage());
        binding.btnNextChapter.setOnClickListener(v -> goToNextPage());
    }

    private void goToPrevPage() {
        if (currentPage <= 1) {
            Toast.makeText(this, "Đang ở trang đầu tiên", Toast.LENGTH_SHORT).show();
            return;
        }
        currentPage--;
        updateDisplay();
        saveReadingProgress();
        binding.readingScrollView.scrollTo(0, 0);
    }

    private void goToNextPage() {
        if (currentPage >= totalPages) {
            Toast.makeText(this, "Đang ở trang cuối cùng", Toast.LENGTH_SHORT).show();
            return;
        }
        currentPage++;
        updateDisplay();
        saveReadingProgress();
        binding.readingScrollView.scrollTo(0, 0);
    }

    private void setupScrollListener() {
        binding.readingScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View scrollChild = binding.readingScrollView.getChildAt(0);
            if (scrollChild == null) return;

            int scrollViewHeight = binding.readingScrollView.getHeight();
            int totalScrollHeight = scrollChild.getHeight();
            int scrollY = binding.readingScrollView.getScrollY();
            int maxScroll = totalScrollHeight - scrollViewHeight;

            if (maxScroll <= 0) return; // Nội dung không cần cuộn

            // Nếu cuộn gần đến cuối trang → có thể tự chuyển trang tiếp (UX tùy chọn)
        });
    }

    // ====================================================================
    // MỤC LỤC (TOC)
    // ====================================================================

    private void showTableOfContents() {
        android.app.Dialog dialog = new android.app.Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.bottom_sheet_toc);

        dialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        View tocContainer = dialog.findViewById(R.id.toc_container);
        dialog.getWindow().getDecorView().setOnTouchListener((v, event) -> {
            if (tocContainer != null) {
                int[] location = new int[2];
                tocContainer.getLocationOnScreen(location);
                float sheetTop = location[1];
                if (event.getY() < sheetTop) {
                    dialog.dismiss();
                    return true;
                }
            }
            return false;
        });

        RecyclerView recyclerView = dialog.findViewById(R.id.toc_recycler);
        if (recyclerView == null) return;

        // Xác định chương hiện tại
        int currentChapterIdx = 0;
        if (currentPage >= 1 && currentPage <= pageToChapterMap.size()) {
            currentChapterIdx = pageToChapterMap.get(currentPage - 1);
        }

        TocAdapter adapter = new TocAdapter(chapterNames);
        adapter.setListener(position -> {
            // Tìm trang đầu tiên của chương được chọn
            goToChapter(position);
            dialog.dismiss();
        });
        adapter.setCurrentChapter(currentChapterIdx + 1);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        int finalIdx = currentChapterIdx;
        recyclerView.post(() ->
                recyclerView.scrollToPosition(Math.max(0, finalIdx)));

        dialog.show();

        // Chiều cao dialog = 90% chiều cao màn hình
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int dialogHeight = (int) (screenHeight * 0.90f);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = dialogHeight;
        params.gravity = android.view.Gravity.BOTTOM;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.show();
    }

    /**
     * Nhảy đến trang đầu tiên của chương chỉ định
     */
    private void goToChapter(int chapterIndex) {
        for (int i = 0; i < pageToChapterMap.size(); i++) {
            if (pageToChapterMap.get(i) == chapterIndex) {
                currentPage = i + 1; // 1-indexed
                updateDisplay();
                saveReadingProgress();
                binding.readingScrollView.scrollTo(0, 0);
                return;
            }
        }
    }

    // ====================================================================
    // CÀI ĐẶT GIAO DIỆN ĐỌC
    // ====================================================================

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
            public void onStopTrackingTouch(SeekBar s) {
                saveCurrentSettings();
            }
        });
    }

    private void setupTextSizeButtons() {
        binding.btnTextMinus.setOnClickListener(v -> changeFontSize(-1));
        binding.btnTextPlus.setOnClickListener(v -> changeFontSize(1));
    }

    private void changeFontSize(int direction) {
        fontSizeIndex = Math.max(0, Math.min(fontSizes.length - 1, fontSizeIndex + direction));
        float size = fontSizes[fontSizeIndex];
        int pct = Math.round((size / fontSizes[3]) * 100);
        binding.btnText100.setText(pct + "%");

        // Cập nhật cỡ chữ cho RecyclerView adapter
        paragraphAdapter.updateTextSize(size);

        // Lưu cài đặt
        saveCurrentSettings();
    }

    private void setupFontButtons() {
        binding.btnFontLiterata.setOnClickListener(v -> {
            currentFont = "literata";
            binding.btnFontLiterata.setBackgroundResource(R.drawable.reading_btn_bg);
            binding.btnFontInter.setBackground(null);
            saveCurrentSettings();
        });

        binding.btnFontInter.setOnClickListener(v -> {
            currentFont = "inter";
            binding.btnFontInter.setBackgroundResource(R.drawable.reading_btn_bg);
            binding.btnFontLiterata.setBackground(null);
            saveCurrentSettings();
        });
    }

    private void applyFontByName(String fontName) {
        if ("inter".equals(fontName)) {
            binding.btnFontInter.setBackgroundResource(R.drawable.reading_btn_bg);
            binding.btnFontLiterata.setBackground(null);
        } else {
            binding.btnFontLiterata.setBackgroundResource(R.drawable.reading_btn_bg);
            binding.btnFontInter.setBackground(null);
        }
    }

    private void setupThemeButtons() {
        binding.btnThemeLight.setOnClickListener(v -> {
            currentTheme = "light";
            applyTheme(R.color.reading_bg, R.color.reading_text,
                    R.color.reading_accent, R.color.reading_text_muted);
            saveCurrentSettings();
        });

        binding.btnThemeSepia.setOnClickListener(v -> {
            currentTheme = "sepia";
            applyTheme(R.color.reading_bg_sepia, R.color.reading_text_sepia,
                    R.color.reading_accent_sepia, R.color.reading_text_muted_sepia);
            saveCurrentSettings();
        });

        binding.btnThemeDark.setOnClickListener(v -> {
            currentTheme = "dark";
            applyTheme(R.color.reading_bg_dark, R.color.reading_text_dark,
                    R.color.reading_accent_dark, R.color.reading_text_muted_dark);
            saveCurrentSettings();
        });
    }

    private void applyThemeByName(String theme) {
        switch (theme) {
            case "sepia":
                applyTheme(R.color.reading_bg_sepia, R.color.reading_text_sepia,
                        R.color.reading_accent_sepia, R.color.reading_text_muted_sepia);
                break;
            case "dark":
                applyTheme(R.color.reading_bg_dark, R.color.reading_text_dark,
                        R.color.reading_accent_dark, R.color.reading_text_muted_dark);
                break;
            default: // "light"
                applyTheme(R.color.reading_bg, R.color.reading_text,
                        R.color.reading_accent, R.color.reading_text_muted);
                break;
        }
    }

    private void applyTheme(int bgColor, int textColor, int accentColor, int mutedColor) {
        int bg = ContextCompat.getColor(this, bgColor);
        int text = ContextCompat.getColor(this, textColor);
        int accent = ContextCompat.getColor(this, accentColor);
        int muted = ContextCompat.getColor(this, mutedColor);

        binding.readingRoot.setBackgroundColor(bg);
        binding.topBar.setBackgroundColor(bg);
        binding.bottomStatusBar.setBackgroundColor(bg);

        binding.tvBookTitle.setTextColor(accent);
        binding.tvChapterLabel.setTextColor(muted);
        binding.tvProgressPercent.setTextColor(accent);
        binding.tvPageNumber.setTextColor(muted);

        // Cập nhật màu cho RecyclerView adapter
        paragraphAdapter.updateColors(text, muted, accent);
    }
}
