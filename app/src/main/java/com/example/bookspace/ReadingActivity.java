package com.example.bookspace;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.bookspace.reader.ReaderQuote;
import com.example.bookspace.repository.SettingsRepository;

import java.util.ArrayList;
import java.util.List;

public class ReadingActivity extends AppCompatActivity {
    public static final String EXTRA_BOOK_ID = "BOOK_ID";
    public static final String EXTRA_SOURCE_PAGE = "SOURCE_PAGE";

    private static final String PREFS_READING = "bookspace_reading";
    private static final String KEY_LAST_BOOK_ID = "last_book_id";
    private static final int INVALID_BOOK_ID = -1;

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
    private int totalChapters = 1;   // Tổng số chương (theo metadata sách)
    private int bookId = INVALID_BOOK_ID;
    private int sourceNavId = R.id.nav_reader; // Tab điều hướng đã mở màn đọc (để khôi phục khi quay lại)
    private String userId = "default_user";
    private String bookTitle = "Sách";
    private String authorName = "";

    // Kindle-style pagination: mỗi "trang" chứa một nhóm paragraph vừa màn hình
    private List<List<BookContent.Paragraph>> allPages; // Danh sách tất cả các trang
    private List<Integer> pageToChapterMap;              // Map: page index → chapter index

    private final int[] fontSizes = {16, 17, 18, 19, 20, 21, 22};
    private int fontSizeIndex = 3;
    private static final int TAB_TOC = 0;
    private static final int TAB_QUOTES = 1;

    // Theme hiện tại
    private String currentTheme = "light";
    private String currentFont = "literata";

    public static Intent createIntent(Context context, int bookId) {
        return createIntent(context, bookId, R.id.nav_reader);
    }

    public static Intent createIntent(Context context, int bookId, int sourceNavId) {
        Intent intent = new Intent(context, ReadingActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        intent.putExtra(EXTRA_SOURCE_PAGE, sourceNavId);
        return intent;
    }

    public static int getLastBookId(Context context) {
        return context.getSharedPreferences(PREFS_READING, Context.MODE_PRIVATE)
                .getInt(KEY_LAST_BOOK_ID, INVALID_BOOK_ID);
    }

    private static void saveLastBookId(Context context, int bookId) {
        context.getSharedPreferences(PREFS_READING, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_LAST_BOOK_ID, bookId)
                .apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        progressDao = db.readingProgressDao();
        userId = SessionManager.getCurrentUserId(this);
        settingsRepo = new SettingsRepository(this);

        if (!readIntentData()) {
            finish();
            return;
        }

        // Setup RecyclerView cho nội dung
        paragraphAdapter = new ParagraphAdapter();
        binding.rvContent.setLayoutManager(new LinearLayoutManager(this));
        binding.rvContent.setAdapter(paragraphAdapter);

        // Tải thông tin sách và nội dung
        if (!loadBookInfo()) {
            finish();
            return;
        }
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

    private boolean readIntentData() {
        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, INVALID_BOOK_ID);
        sourceNavId = getIntent().getIntExtra(EXTRA_SOURCE_PAGE, R.id.nav_reader);
        if (bookId <= 0) {
            Toast.makeText(this, "Không tìm thấy sách để đọc", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ====================================================================
    // LOAD DỮ LIỆU
    // ====================================================================

    private boolean loadBookInfo() {
        BookEntity bookEntity = db.bookDao().getBookById(bookId);
        if (bookEntity != null) {
            bookTitle = trimToEmpty(bookEntity.title);
            authorName = trimToEmpty(bookEntity.author);
            totalChapters = bookEntity.pages;
            saveLastBookId(this, bookId);
            return true;
        }
        Toast.makeText(this, "Không tìm thấy dữ liệu sách", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Tải nội dung sách từ file .txt trong assets
     * và phân trang theo kiểu Kindle (mỗi trang ~3-4 paragraph)
     */
    private void loadBookContent() {
        BookEntity bookEntity = db.bookDao().getBookById(bookId);
        String bookFilePath = resolveBookFilePath(bookEntity);

        if (!TextUtils.isEmpty(bookFilePath)) {
            // Parse file sách thật
            bookContent = BookTextParser.parse(this, bookFilePath);
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

    private String resolveBookFilePath(BookEntity bookEntity) {
        if (bookEntity == null) {
            return "";
        }

        String filePath = trimToEmpty(bookEntity.bookFilePath);
        if (!TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        String title = trimToEmpty(bookEntity.title);
        if ("Đắc Nhân Tâm".equalsIgnoreCase(title)) {
            return "books/dac_nhan_tam.txt";
        }
        if ("Nhà Giả Kim".equalsIgnoreCase(title)) {
            return "books/nha_gia_kim.txt";
        }
        return "";
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
        ReadingProgressEntity progress = progressDao.getProgress(userId, bookId);
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
        if (bookId <= 0) {
            return;
        }
        ReadingProgressEntity progress = new ReadingProgressEntity();
        progress.userId = userId;
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
        Dialog dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.bottom_sheet_toc);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        }

        View tocContainer = dialog.findViewById(R.id.toc_container);
        if (dialog.getWindow() != null) {
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
        }

        RecyclerView tocRecyclerView = dialog.findViewById(R.id.toc_recycler);
        RecyclerView quoteRecyclerView = dialog.findViewById(R.id.quote_recycler);
        TextView quoteEmptyText = dialog.findViewById(R.id.quote_empty_text);
        TextView btnTabToc = dialog.findViewById(R.id.btn_tab_toc);
        TextView btnTabQuotes = dialog.findViewById(R.id.btn_tab_quotes);
        if (tocRecyclerView == null || quoteRecyclerView == null
                || quoteEmptyText == null || btnTabToc == null || btnTabQuotes == null) {
            return;
        }

        // Xác định chương hiện tại
        int currentChapterIdx = 0;
        if (currentPage >= 1 && currentPage <= pageToChapterMap.size()) {
            currentChapterIdx = pageToChapterMap.get(currentPage - 1);
        }

        tocAdapter = new TocAdapter(chapterNames);
        tocAdapter.setListener(position -> {
            // Tìm trang đầu tiên của chương được chọn
            goToChapter(position);
            dialog.dismiss();
        });
        tocAdapter.setCurrentChapter(currentChapterIdx + 1);

        tocRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tocRecyclerView.setAdapter(tocAdapter);
        int finalIdx = currentChapterIdx;
        tocRecyclerView.post(() ->
                tocRecyclerView.scrollToPosition(Math.max(0, finalIdx)));

        List<ReaderQuote> quotes = collectReaderQuotes();
        QuoteListAdapter quoteAdapter = new QuoteListAdapter(quotes);
        setupQuoteActions(quoteAdapter, dialog);
        quoteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        quoteRecyclerView.setAdapter(quoteAdapter);

        btnTabToc.setOnClickListener(v -> showTocTab(
                TAB_TOC,
                tocRecyclerView,
                quoteRecyclerView,
                quoteEmptyText,
                btnTabToc,
                btnTabQuotes,
                !quotes.isEmpty()
        ));
        btnTabQuotes.setOnClickListener(v -> showTocTab(
                TAB_QUOTES,
                tocRecyclerView,
                quoteRecyclerView,
                quoteEmptyText,
                btnTabToc,
                btnTabQuotes,
                !quotes.isEmpty()
        ));
        showTocTab(
                TAB_TOC,
                tocRecyclerView,
                quoteRecyclerView,
                quoteEmptyText,
                btnTabToc,
                btnTabQuotes,
                !quotes.isEmpty()
        );

        // Chiều cao dialog = 90% chiều cao màn hình
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int dialogHeight = (int) (screenHeight * 0.90f);

        dialog.show();

        if (dialog.getWindow() != null) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = dialogHeight;
            params.gravity = android.view.Gravity.BOTTOM;
            dialog.getWindow().setAttributes(params);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void showTocTab(int selectedTab,
                            RecyclerView tocRecyclerView,
                            RecyclerView quoteRecyclerView,
                            TextView quoteEmptyText,
                            TextView btnTabToc,
                            TextView btnTabQuotes,
                            boolean hasQuotes) {
        boolean showQuotes = selectedTab == TAB_QUOTES;
        tocRecyclerView.setVisibility(showQuotes ? View.GONE : View.VISIBLE);
        quoteRecyclerView.setVisibility(showQuotes && hasQuotes ? View.VISIBLE : View.GONE);
        quoteEmptyText.setVisibility(showQuotes && !hasQuotes ? View.VISIBLE : View.GONE);

        updateTocTabButton(btnTabToc, !showQuotes);
        updateTocTabButton(btnTabQuotes, showQuotes);
    }

    private void updateTocTabButton(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.reading_btn_bg : 0);
        tab.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.on_surface : R.color.on_surface_variant));
    }

    private void setupQuoteActions(QuoteListAdapter quoteAdapter, Dialog dialog) {
        quoteAdapter.setListener(quote -> {
            if (openQuoteCard(quote)) {
                dialog.dismiss();
            }
        });
    }

    private boolean openQuoteCard(ReaderQuote quote) {
        String quoteText = getSafeQuoteText(quote);
        if (TextUtils.isEmpty(quoteText)) {
            Toast.makeText(this, R.string.reader_quote_empty_toast, Toast.LENGTH_SHORT).show();
            return false;
        }

        startActivity(QuoteCardActivity.createIntent(
                this,
                quoteText,
                getSafeBookTitle(),
                getSafeAuthorName(),
                getSafeChapterName(quote),
                getSafeChapterIndex(quote),
                getSafePageNumber(quote)
        ));
        return true;
    }

    private List<ReaderQuote> collectReaderQuotes() {
        List<ReaderQuote> quotes = new ArrayList<>();
        if (allPages == null) {
            return quotes;
        }

        for (int pageIndex = 0; pageIndex < allPages.size(); pageIndex++) {
            List<BookContent.Paragraph> paragraphs = allPages.get(pageIndex);
            if (paragraphs == null) {
                continue;
            }

            int chapterIndex = getChapterIndexForPage(pageIndex);
            String chapterName = getSafeChapterName(chapterIndex);
            for (BookContent.Paragraph paragraph : paragraphs) {
                if (paragraph != null && paragraph.getType() == BookContent.Paragraph.TYPE_QUOTE) {
                    quotes.add(new ReaderQuote(
                            paragraph.getText(),
                            chapterName,
                            chapterIndex,
                            pageIndex + 1
                    ));
                }
            }
        }

        return quotes;
    }

    private int getChapterIndexForPage(int pageIndex) {
        if (pageToChapterMap != null && pageIndex >= 0 && pageIndex < pageToChapterMap.size()) {
            return pageToChapterMap.get(pageIndex);
        }
        return -1;
    }

    private String getSafeChapterName(int chapterIndex) {
        if (chapterNames != null && chapterIndex >= 0 && chapterIndex < chapterNames.size()) {
            return trimToEmpty(chapterNames.get(chapterIndex));
        }
        return "";
    }

    private String getSafeQuoteText(ReaderQuote quote) {
        if (quote == null) {
            return "";
        }
        return trimToEmpty(quote.getText());
    }

    private String getSafeChapterName(ReaderQuote quote) {
        if (quote == null) {
            return "";
        }
        return trimToEmpty(quote.getChapterName());
    }

    private int getSafeChapterIndex(ReaderQuote quote) {
        if (quote == null) {
            return -1;
        }
        return quote.getChapterIndex();
    }

    private int getSafePageNumber(ReaderQuote quote) {
        if (quote == null) {
            return -1;
        }
        return quote.getPageNumber();
    }

    private String getSafeBookTitle() {
        return trimToEmpty(bookTitle);
    }

    private String getSafeAuthorName() {
        return trimToEmpty(authorName);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
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
