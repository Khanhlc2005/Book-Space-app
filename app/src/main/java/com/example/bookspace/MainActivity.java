package com.example.bookspace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.bumptech.glide.Glide;
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
        setupDrawerMenu();
        setupBottomNav();
        setupRecyclerViews();
        setupSearch();
        seedDatabase();
    }

    private void seedDatabase() {
        if (bookRepository.getAllBooks().size() < 14) {
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
                dao.insertAll(sampleBooks);
                runOnUiThread(() -> {
                    setupRecyclerViews();
                    setupFeaturedViewPager();
                });
            });
        }
    }

    private BookEntity createBookEntity(String title, String author, String cover, int pages, String desc, String cat) {
        return createBookEntity(title, author, cover, pages, desc, cat, null);
    }

    private BookEntity createBookEntity(String title, String author, String cover, int pages, String desc, String cat, String bookFilePath) {
        BookEntity b = new BookEntity();
        b.title = title; b.author = author; b.coverUrl = cover; b.pages = pages; b.description = desc; b.category = cat;
        b.isDownloaded = false;
        b.bookFilePath = bookFilePath;
        return b;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
        int activeColor = ContextCompat.getColor(this, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);
        updateBottomNavUi(currentNavId, activeColor, inactiveColor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    private void setupStaticUI() {
        String urlProfile = "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP";
        Glide.with(this).load(urlProfile).circleCrop().into(binding.imgProfile);

        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.main.requestFocus();
    }

    private void setupDrawerMenu() {
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            String category = null;

            if (id == R.id.nav_cat_all) category = getString(R.string.cat_all);
            else if (id == R.id.nav_cat_life_skills) category = getString(R.string.cat_life_skills);
            else if (id == R.id.nav_cat_psychology) category = getString(R.string.cat_psychology);
            else if (id == R.id.nav_cat_classic) category = getString(R.string.cat_classic);
            else if (id == R.id.nav_cat_economics) category = getString(R.string.cat_economics);
            else if (id == R.id.nav_cat_literature) category = getString(R.string.cat_literature);

            if (category != null) {
                Intent intent = new Intent(this, CategoryActivity.class);
                intent.putExtra("CATEGORY_NAME", category);
                startActivity(intent);
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
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
        binding.vpFeaturedBooks.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        binding.vpFeaturedBooks.setPageTransformer(transformer);
    }

    private void setupRecyclerViews() {
        // Lấy tất cả sách từ DB hiển thị ở "Mới cập nhật"
        List<BookEntity> allBooks = bookRepository.getAllBooks();
        List<Book> listRecent = new ArrayList<>();
        for (BookEntity entity : allBooks) listRecent.add(Book.fromEntity(entity));

        binding.rvRecentlyUpdated.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvRecentlyUpdated.setAdapter(new BookAdapter(listRecent, this));

        // Lọc sách "Tiểu thuyết" từ DB
        List<BookEntity> novelEntities = bookRepository.getByCategory("TIỂU THUYẾT");
        List<Book> listNovel = new ArrayList<>();
        for (BookEntity e : novelEntities) {
            listNovel.add(Book.fromEntity(e));
        }

        binding.rvNovels.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvNovels.setAdapter(new BookAdapter(listNovel, this));
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
                    binding.recyclerBooks.setVisibility(View.GONE);
                    binding.emptyStateSearch.getRoot().setVisibility(View.GONE);
                } else {
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
        int activeColor = ContextCompat.getColor(this, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        binding.bottomNav.navHome.setOnClickListener(v -> {
            if (currentNavId != R.id.nav_home) {
                currentNavId = R.id.nav_home;
                updateBottomNavUi(R.id.nav_home, activeColor, inactiveColor);
            }
        });
        binding.bottomNav.navReader.setOnClickListener(v -> {
            if (currentNavId != R.id.nav_reader) {
                currentNavId = R.id.nav_reader;
                updateBottomNavUi(R.id.nav_reader, activeColor, inactiveColor);
            }
            startActivity(new Intent(this, ReadingActivity.class));
        });
        binding.bottomNav.navLibrary.setOnClickListener(v -> {
            if (currentNavId != R.id.nav_library) {
                currentNavId = R.id.nav_library;
                updateBottomNavUi(R.id.nav_library, activeColor, inactiveColor);
            }
            startActivity(new Intent(this, FavouritesActivity.class));
        });
    }

    private void updateBottomNavUi(int activeId, int activeColor, int inactiveColor) {
        binding.bottomNav.iconHome.setColorFilter(activeId == R.id.nav_home ? activeColor : inactiveColor);
        binding.bottomNav.textHome.setTextColor(activeId == R.id.nav_home ? activeColor : inactiveColor);
        binding.bottomNav.navHome.setBackgroundResource(activeId == R.id.nav_home ? R.drawable.bottom_nav_active_bg : 0);

        binding.bottomNav.iconLibrary.setColorFilter(activeId == R.id.nav_library ? activeColor : inactiveColor);
        binding.bottomNav.textLibrary.setTextColor(activeId == R.id.nav_library ? activeColor : inactiveColor);
        binding.bottomNav.navLibrary.setBackgroundResource(activeId == R.id.nav_library ? R.drawable.bottom_nav_active_bg : 0);

        binding.bottomNav.iconReader.setColorFilter(activeId == R.id.nav_reader ? activeColor : inactiveColor);
        binding.bottomNav.textReader.setTextColor(activeId == R.id.nav_reader ? activeColor : inactiveColor);
        binding.bottomNav.navReader.setBackgroundResource(activeId == R.id.nav_reader ? R.drawable.bottom_nav_active_bg : 0);
    }

    /**
     * Tô sáng tab điều hướng dưới cùng cho BẤT KỲ activity nào include layout_bottom_nav.xml.
     * Dùng findViewById để hoạt động chung, không phụ thuộc binding của từng màn.
     */
    public static void updateBottomNavIcon(Activity activity, int activeId) {
        int activeColor = ContextCompat.getColor(activity, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(activity, R.color.nav_inactive);
        applyNavItem(activity, R.id.nav_home, R.id.icon_home, R.id.text_home, activeId == R.id.nav_home, activeColor, inactiveColor);
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
        View container = activity.findViewById(containerId);
        if (container != null) {
            container.setBackgroundResource(isActive ? R.drawable.bottom_nav_active_bg : 0);
        }
    }

    @Override
    public void onBookClick(Book book) {
        BookDetailBottomSheet.show(this, book);
    }
}
