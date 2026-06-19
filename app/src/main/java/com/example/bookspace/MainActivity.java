package com.example.bookspace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.BookDao;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.databinding.ActivityMainBinding;
import com.example.bookspace.repository.BookRepository;
import com.example.bookspace.repository.ProgressRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnBookClickListener {
    private ActivityMainBinding binding;
    private BookRepository bookRepository;
    private int currentNavId = R.id.nav_home;
    private int currentSideMenuItemId = R.id.nav_cat_all;
    private String currentCategory = "";
    private boolean showingCategory = false;
    private final Handler sliderHandler = new Handler();
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null) return;
            if (binding.vpFeaturedBooks.getAdapter() != null) {
                int currentItem = binding.vpFeaturedBooks.getCurrentItem();
                int totalItems = binding.vpFeaturedBooks.getAdapter().getItemCount();
                if (totalItems > 0) {
                    binding.vpFeaturedBooks.setCurrentItem((currentItem + 1) % totalItems, true);
                }
            }
            sliderHandler.postDelayed(this, 3000);
        }
    };
    private BookAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isLoggedIn(this)) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookRepository = new BookRepository(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBar.setPadding(
                    binding.topBar.getPaddingLeft(),
                    insets.top + 16,
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

        setupStaticUI();
        setupFeaturedViewPager();
        setupSideMenu();
        setupBottomNav();
        setupRecyclerViews();
        setupSearch();
        showHomeContent();
        seedDatabase();
    }

    private void seedDatabase() {
        if (bookRepository.getAllBooks().size() < 18) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                BookDao dao = AppDatabase.getInstance(this).bookDao();
                List<BookEntity> sampleBooks = new ArrayList<>();
                sampleBooks.add(createBookEntity("Nhà Giả Kim", "Paulo Coelho", "https://picsum.photos/600/400?random=1", 225, "Một câu chuyện cổ tích giản dị, nhân ái, giàu chất thơ...", "VĂN HỌC", "books/nha_gia_kim.txt"));
                sampleBooks.add(createBookEntity("Đắc Nhân Tâm", "Dale Carnegie", "https://picsum.photos/600/400?random=2", 320, "Nghệ thuật thu phục lòng người...", "KỸ NĂNG SỐNG", "books/dac_nhan_tam.txt"));
                sampleBooks.add(createBookEntity("Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", "https://picsum.photos/600/400?random=3", 285, "Những kinh nghiệm thực tế của tác giả trên hành trình tuổi trẻ...", "KỸ NĂNG SỐNG"));
                sampleBooks.add(createBookEntity("Sapiens: Lược sử loài người", "Yuval Noah Harari", "https://picsum.photos/600/400?random=4", 512, "Lịch sử tiến hóa của loài người từ thuở sơ khai...", "KINH ĐIỂN"));
                sampleBooks.add(createBookEntity("Tâm Lý Học Tội Phạm", "Khương Luật", "https://picsum.photos/600/400?random=5", 350, "Phân tích tâm lý học tội phạm qua các vụ án có thật...", "TÂM LÝ"));
                sampleBooks.add(createBookEntity("Cha Giàu Cha Nghèo", "Robert Kiyosaki", "https://picsum.photos/600/400?random=6", 360, "Bài học về giáo dục tài chính và đầu tư...", "KINH TẾ"));
                sampleBooks.add(createBookEntity("Cây Cam Ngọt Của Tôi", "José Mauro de Vasconcelos", "https://picsum.photos/600/400?random=7", 244, "Câu chuyện cảm động về cậu bé Zezé...", "VĂN HỌC"));
                sampleBooks.add(createBookEntity("Nghĩ Giàu Làm Giàu", "Napoleon Hill", "https://picsum.photos/600/400?random=8", 400, "13 nguyên tắc nghĩ giàu làm giàu...", "KINH TẾ"));
                sampleBooks.add(createBookEntity("Sức Mạnh Của Thói Quen", "Charles Duhigg", "https://picsum.photos/600/400?random=9", 380, "Cách tạo thói quen tốt và loại bỏ thói quen xấu...", "KỸ NĂNG SỐNG"));
                sampleBooks.add(createBookEntity("Muôn Kiếp Nhân Sinh", "Nguyên Phong", "https://picsum.photos/600/400?random=10", 420, "Những câu chuyện tiền kiếp và luật nhân quả...", "TÂM LÝ"));
                sampleBooks.add(createBookEntity("Quẳng Gánh Lo Đi Và Vui Sống", "Dale Carnegie", "https://picsum.photos/600/400?random=11", 340, "Những nguyên tắc giúp người đọc quản lý lo âu và sống nhẹ nhàng hơn.", "KỸ NĂNG SỐNG"));
                sampleBooks.add(createBookEntity("Veronika Quyết Chết", "Paulo Coelho", "https://picsum.photos/600/400?random=12", 256, "Một hành trình nhìn lại ý nghĩa sống qua lựa chọn và tự do cá nhân.", "VĂN HỌC"));
                sampleBooks.add(createBookEntity("Homo Deus", "Yuval Noah Harari", "https://picsum.photos/600/400?random=13", 480, "Tác giả tiếp tục đặt câu hỏi về tương lai nhân loại và công nghệ.", "KINH ĐIỂN"));
                sampleBooks.add(createBookEntity("Hành Trình Về Phương Đông", "Nguyên Phong", "https://picsum.photos/600/400?random=14", 320, "Những ghi chép về văn hóa, triết học và trải nghiệm tâm linh phương Đông.", "TÂM LÝ"));
                sampleBooks.add(createBookEntity("Lược Sử Thời Gian", "Stephen Hawking", "https://picsum.photos/600/400?random=15", 256, "Khám phá vũ trụ, thời gian và những câu hỏi lớn của khoa học hiện đại.", "KHOA HỌC"));
                sampleBooks.add(createBookEntity("Vũ Trụ Trong Vỏ Hạt Dẻ", "Stephen Hawking", "https://picsum.photos/600/400?random=16", 288, "Một cách tiếp cận dễ hiểu hơn về vũ trụ học và vật lý hiện đại.", "KHOA HỌC"));
                sampleBooks.add(createBookEntity("Việt Nam Sử Lược", "Trần Trọng Kim", "https://picsum.photos/600/400?random=17", 420, "Tổng quan lịch sử Việt Nam qua các thời kỳ quan trọng.", "LỊCH SỬ"));
                sampleBooks.add(createBookEntity("Súng, Vi Trùng Và Thép", "Jared Diamond", "https://picsum.photos/600/400?random=18", 496, "Góc nhìn lịch sử và địa lý về sự phát triển của các nền văn minh.", "LỊCH SỬ"));
                for (BookEntity book : sampleBooks) {
                    if (dao.findByTitleAndAuthor(book.title, book.author) == null) {
                        dao.insert(book);
                    }
                }
                runOnUiThread(() -> {
                    setupRecyclerViews();
                    setupFeaturedViewPager();
                    if (showingCategory) {
                        showCategoryContent(currentCategory, currentSideMenuItemId);
                    }
                });
            });
        }
    }

    private BookEntity createBookEntity(String title, String author, String cover, int pages, String desc, String cat) {
        return createBookEntity(title, author, cover, pages, desc, cat, null);
    }

    private BookEntity createBookEntity(String title, String author, String cover, int pages, String desc, String cat, String bookFilePath) {
        BookEntity b = new BookEntity();
        b.title = title;
        b.author = author;
        b.coverUrl = cover;
        b.pages = pages;
        b.description = desc;
        b.category = cat;
        b.isDownloaded = false;
        b.bookFilePath = bookFilePath;
        return b;
    }

    private void setupStaticUI() {
        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.main.requestFocus();
    }

    private void setupSideMenu() {
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_cat_all) {
                showHomeContent();
            } else if (id == R.id.nav_challenges) {
                startActivity(new Intent(this, ChallengeActivity.class));
            } else {
                String category = getCategoryForMenuItem(id);
                if (!category.isEmpty()) {
                    onCategorySelected(category, id);
                }
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        updateSideMenuSelection(R.id.nav_cat_all);
    }

    private void setupFeaturedViewPager() {
        List<BookEntity> featuredEntities = bookRepository.getAllBooks();
        List<Book> listFeatured = new ArrayList<>();
        for (int i = 0; i < Math.min(5, featuredEntities.size()); i++) {
            listFeatured.add(Book.fromEntity(featuredEntities.get(i)));
        }
        FeaturedBookAdapter adapter = new FeaturedBookAdapter(listFeatured, this);
        binding.vpFeaturedBooks.setAdapter(adapter);
        binding.vpFeaturedBooks.setOffscreenPageLimit(3);
        View child = binding.vpFeaturedBooks.getChildAt(0);
        if (child instanceof RecyclerView) {
            child.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        }
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        binding.vpFeaturedBooks.setPageTransformer(transformer);
    }

    private void setupRecyclerViews() {
        List<Book> allBooks = toBooks(bookRepository.getAllBooks());

        setupBookSection(binding.rvRecentlyUpdated, allBooks);

        List<Book> literatureBooks = toBooks(bookRepository.getByCategory("VĂN HỌC"));
        if (literatureBooks.isEmpty()) {
            literatureBooks = toBooks(bookRepository.getByCategory("TIỂU THUYẾT"));
        }
        setupBookSection(binding.rvNovels, literatureBooks);
        setupBookSection(binding.rvLearningBooks, getLearningBooks(allBooks));
        setupBookSection(binding.rvRecommendedBooks, getRecommendedBooks(allBooks));
    }

    private void setupBookSection(RecyclerView recyclerView, List<Book> books) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(new BookAdapter(books == null ? new ArrayList<>() : books, this));
    }

    private List<Book> toBooks(List<BookEntity> entities) {
        List<Book> books = new ArrayList<>();
        if (entities == null) {
            return books;
        }
        for (BookEntity entity : entities) {
            if (entity != null) {
                books.add(Book.fromEntity(entity));
            }
        }
        return books;
    }

    private List<Book> getLearningBooks(List<Book> allBooks) {
        List<Book> books = new ArrayList<>();
        if (allBooks == null) {
            return books;
        }
        for (Book book : allBooks) {
            String category = normalizeText(book == null ? "" : book.getCategory());
            if (category.contains("KỸ NĂNG")
                    || category.contains("KHOA HỌC")
                    || category.contains("TÂM LÝ")) {
                books.add(book);
            }
        }
        return books;
    }

    private List<Book> getRecommendedBooks(List<Book> allBooks) {
        List<Book> books = new ArrayList<>();
        if (allBooks == null) {
            return books;
        }
        for (int i = 0; i < allBooks.size(); i++) {
            Book book = allBooks.get(i);
            if (book != null && i % 2 == 0) {
                books.add(book);
            }
        }
        if (books.isEmpty()) {
            books.addAll(allBooks);
        }
        return books;
    }

    private void onCategorySelected(String category, int menuItemId) {
        showCategoryContent(category, menuItemId);
    }

    private void showHomeContent() {
        showingCategory = false;
        currentCategory = "";
        currentSideMenuItemId = R.id.nav_cat_all;
        binding.txtAppTitle.setText(R.string.app_title);
        binding.btnMenu.setImageResource(R.drawable.ic_toc);
        binding.btnMenu.setContentDescription(getString(R.string.cd_menu));
        binding.btnMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.searchBarCard.setVisibility(View.VISIBLE);
        setHomeSectionsVisible(true);
        binding.categoryContent.setVisibility(View.GONE);
        binding.emptyStateCategory.getRoot().setVisibility(View.GONE);
        updateSideMenuSelection(R.id.nav_cat_all);
    }

    private void showCategoryContent(String category, int menuItemId) {
        showingCategory = true;
        currentCategory = category == null ? "" : category.trim();
        currentSideMenuItemId = menuItemId;
        binding.txtAppTitle.setText(currentCategory);
        binding.btnMenu.setImageResource(R.drawable.ic_arrow_back);
        binding.btnMenu.setContentDescription(getString(R.string.cd_back));
        binding.btnMenu.setOnClickListener(v -> showHomeContent());
        binding.searchInput.setText("");
        binding.searchBarCard.setVisibility(View.GONE);
        binding.searchSuggestionsContainer.setVisibility(View.GONE);
        binding.recyclerBooks.setVisibility(View.GONE);
        binding.emptyStateSearch.getRoot().setVisibility(View.GONE);
        setHomeSectionsVisible(false);
        binding.categoryContent.setVisibility(View.VISIBLE);
        updateSideMenuSelection(menuItemId);

        List<Book> books = toBooks(bookRepository.getByCategory(normalizeCategoryForQuery(currentCategory)));
        setupCategoryGrid(books);
        updateCategoryEmptyState(books.isEmpty());
    }

    private void setupCategoryGrid(List<Book> books) {
        binding.rvCategoryBooks.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvCategoryBooks.setAdapter(new BookAdapter(books == null ? new ArrayList<>() : books, this, true));
    }

    private void updateCategoryEmptyState(boolean empty) {
        binding.rvCategoryBooks.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyStateCategory.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            binding.emptyStateCategory.imgEmptyIcon.setImageResource(R.drawable.ic_menu_book);
            binding.emptyStateCategory.txtEmptyMessage.setText(R.string.category_empty);
        }
    }

    private void setHomeSectionsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        binding.txtSectionFeatured.setVisibility(visibility);
        binding.vpFeaturedBooks.setVisibility(visibility);
        binding.txtSectionRecent.setVisibility(visibility);
        binding.rvRecentlyUpdated.setVisibility(visibility);
        binding.txtSectionCategory.setVisibility(visibility);
        binding.rvNovels.setVisibility(visibility);
        binding.txtSectionLearn.setVisibility(visibility);
        binding.rvLearningBooks.setVisibility(visibility);
        binding.txtSectionRecommended.setVisibility(visibility);
        binding.rvRecommendedBooks.setVisibility(visibility);
    }

    private void updateSideMenuSelection(int menuItemId) {
        MenuItem item = binding.navigationView.getMenu().findItem(menuItemId);
        if (item != null) {
            item.setChecked(true);
        }
    }

    private String getCategoryForMenuItem(int itemId) {
        if (itemId == R.id.nav_cat_life_skills) return getString(R.string.cat_life_skills);
        if (itemId == R.id.nav_cat_psychology) return getString(R.string.cat_psychology);
        if (itemId == R.id.nav_cat_classic) return getString(R.string.cat_classic);
        if (itemId == R.id.nav_cat_science) return getString(R.string.cat_science);
        if (itemId == R.id.nav_cat_history) return getString(R.string.cat_history);
        if (itemId == R.id.nav_cat_economics) return getString(R.string.cat_economics);
        if (itemId == R.id.nav_cat_literature) return getString(R.string.cat_literature);
        return "";
    }

    private String normalizeCategoryForQuery(String category) {
        return normalizeText(category);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private void setupSearch() {
        binding.recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new BookAdapter(new ArrayList<>(), true, this);
        binding.recyclerBooks.setAdapter(searchAdapter);
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    binding.searchSuggestionsContainer.setVisibility(View.GONE);
                    binding.recyclerBooks.setVisibility(View.GONE);
                    binding.emptyStateSearch.getRoot().setVisibility(View.GONE);
                } else {
                    binding.searchSuggestionsContainer.setVisibility(View.VISIBLE);
                    List<BookEntity> entities = bookRepository.searchBooks(keyword);
                    List<Book> filtered = new ArrayList<>();
                    for (BookEntity e : entities) filtered.add(Book.fromEntity(e));
                    searchAdapter.updateData(filtered);
                    if (filtered.isEmpty()) {
                        binding.recyclerBooks.setVisibility(View.GONE);
                        binding.emptyStateSearch.getRoot().setVisibility(View.VISIBLE);
                        binding.emptyStateSearch.imgEmptyIcon.setImageResource(android.R.drawable.ic_menu_search);
                        binding.emptyStateSearch.txtEmptyMessage.setText(String.format(Locale.getDefault(), "Không tìm thấy kết quả cho '%s'", keyword));
                    } else {
                        binding.recyclerBooks.setVisibility(View.VISIBLE);
                        binding.emptyStateSearch.getRoot().setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBottomNav() {
        binding.bottomNav.navHome.setOnClickListener(v -> {
            currentNavId = R.id.nav_home;
            updateBottomNavSelection(R.id.nav_home);
            showHomeContent();
        });
        binding.bottomNav.navChallenge.setOnClickListener(v -> {
            startActivity(new Intent(this, ChallengeActivity.class));
        });
        binding.bottomNav.navReader.setOnClickListener(v -> openLastReadingBook());
        binding.bottomNav.navLibrary.setOnClickListener(v -> {
            currentNavId = R.id.nav_library;
            updateBottomNavSelection(R.id.nav_library);
            startActivity(new Intent(this, FavouritesActivity.class));
        });
    }

    private void openLastReadingBook() {
        int lastBookId = getLastReadableBookId();
        if (lastBookId > 0) {
            startActivity(ReadingActivity.createIntent(this, lastBookId, R.id.nav_home));
            return;
        }

        Toast.makeText(this, "Chưa có sách đang đọc", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, CurrentlyReadingListActivity.class));
    }

    private int getLastReadableBookId() {
        int lastBookId = ReadingActivity.getLastBookId(this);
        if (isValidBookId(lastBookId)) {
            return lastBookId;
        }

        ProgressRepository progressRepository = new ProgressRepository(this);
        List<BookEntity> booksInProgress = progressRepository.getBooksInReadingProgress();
        if (booksInProgress != null && !booksInProgress.isEmpty()) {
            return booksInProgress.get(0).id;
        }
        return -1;
    }

    private boolean isValidBookId(int bookId) {
        return bookId > 0 && bookRepository.getBookById(bookId) != null;
    }

    private void updateBottomNavSelection(int activeId) {
        updateBottomNavIcon(this, activeId);
    }

    /**
     * Tô sáng tab điều hướng dưới cùng cho BẤT KỲ activity nào include layout_bottom_nav.xml.
     * Dùng findViewById để hoạt động chung, không phụ thuộc binding của từng màn.
     */
    public static void updateBottomNavIcon(Activity activity, int activeId) {
        int activeColor = ContextCompat.getColor(activity, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(activity, R.color.nav_inactive);
        applyNavItem(activity, R.id.nav_home, R.id.icon_home, R.id.text_home, activeId == R.id.nav_home, activeColor, inactiveColor);
        applyNavItem(activity, R.id.nav_challenge, R.id.icon_challenge, R.id.text_challenge, activeId == R.id.nav_challenge, activeColor, inactiveColor);
        applyNavItem(activity, R.id.nav_reader, R.id.icon_reader, R.id.text_reader, activeId == R.id.nav_reader, activeColor, inactiveColor);
        applyNavItem(activity, R.id.nav_library, R.id.icon_library, R.id.text_library, activeId == R.id.nav_library, activeColor, inactiveColor);
    }

    private static void applyNavItem(Activity activity, int containerId, int iconId, int textId,
                                     boolean isActive, int activeColor, int inactiveColor) {
        int color = isActive ? activeColor : inactiveColor;
        ImageView icon = activity.findViewById(iconId);
        if (icon != null) icon.setColorFilter(color);
        TextView text = activity.findViewById(textId);
        if (text != null) text.setTextColor(color);
    }

    @Override
    public void onBookClick(Book book) {
        BookDetailBottomSheet.show(this, book);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
        updateBottomNavSelection(currentNavId);
    }
}
