package com.example.bookspace;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.databinding.ActivityMainBinding;
import com.example.bookspace.repository.BookRepository;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.bookspace.database.AppDatabase;
import com.example.bookspace.database.dao.BookDao;
import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.databinding.ActivityMainBinding;
import com.example.bookspace.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnBookClickListener {
    private ActivityMainBinding binding;
    private BookRepository repo;
    private static final String TAG = "MainActivity";
    private BookRepository bookRepository;
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable = new Runnable() {
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

        // Khởi tạo Repository
        bookRepository = new BookRepository(this);

        // System Bar Insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBar.setPadding(binding.topBar.getPaddingLeft(), insets.top + 16, binding.topBar.getPaddingRight(), binding.topBar.getPaddingBottom());
            binding.bottomNavContainer.setPadding(binding.bottomNavContainer.getPaddingLeft(), binding.bottomNavContainer.getPaddingTop(), binding.bottomNavContainer.getPaddingRight(), insets.bottom + 24);
            return WindowInsetsCompat.CONSUMED;
        });

        repo = new BookRepository(this);

        setupStaticUI();
        setupFeaturedViewPager();
        setupDrawerMenu(); // Menu bên trái thay thế cho Chips
        setupBottomNav();
        setupRecyclerViews();
        setupSearch();
        seedDatabase();
    }

    private void seedDatabase() {
        if (repo.getAllBooks().size() < 14) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                BookDao dao = AppDatabase.getInstance(this).bookDao();
                List<BookEntity> sampleBooks = new ArrayList<>();
                sampleBooks.add(createBookEntity("Nhà Giả Kim", "Paulo Coelho", "https://picsum.photos/600/400?random=1", 225, "Một câu chuyện cổ tích giản dị, nhân ái, giàu chất thơ...", "VĂN HỌC"));
                sampleBooks.add(createBookEntity("Đắc Nhân Tâm", "Dale Carnegie", "https://picsum.photos/600/400?random=2", 320, "Nghệ thuật thu phục lòng người...", "KỸ NĂNG SỐNG"));
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
        BookEntity b = new BookEntity();
        b.title = title; b.author = author; b.coverUrl = cover; b.pages = pages; b.description = desc; b.category = cat;
        b.isDownloaded = false;
        return b;
    }

    @Override protected void onResume() { super.onResume(); sliderHandler.postDelayed(sliderRunnable, 3000); }
    @Override protected void onPause() { super.onPause(); sliderHandler.removeCallbacks(sliderRunnable); }

    private void setupStaticUI() {
        String urlProfile = "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP";
        Glide.with(this).load(urlProfile).circleCrop().into(binding.imgProfile);

        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
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
        // Lấy tất cả sách từ DB để hiển thị trên Featured slider
        List<BookEntity> entities = bookRepository.getAllBooks();
        List<Book> listFeatured = new ArrayList<>();
        for (BookEntity e : entities) {
            Book book = new Book(e.coverUrl, e.title, e.author, e.pages, e.description, e.category);
            book.setId(e.id);
            listFeatured.add(book);
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
        List<BookEntity> allEntities = bookRepository.getAllBooks();
        List<Book> listRecent = new ArrayList<>();
        for (BookEntity e : allEntities) {
            Book book = new Book(e.coverUrl, e.title, e.author, e.pages, e.description, e.category);
            book.setId(e.id);
            listRecent.add(book);
        }
        for (BookEntity entity : allBooks) listRecent.add(Book.fromEntity(entity));

        binding.rvNovels.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvNovels.setAdapter(new BookAdapter(listRecent, this));

        // Lọc sách "Tiểu thuyết" từ DB
        List<BookEntity> novelEntities = bookRepository.getByCategory("TIỂU THUYẾT");
        List<Book> listNovel = new ArrayList<>();
        for (BookEntity e : novelEntities) {
            Book book = new Book(e.coverUrl, e.title, e.author, e.pages, e.description, e.category);
            book.setId(e.id);
            listNovel.add(book);
        }

        binding.rvRecentlyUpdated.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvRecentlyUpdated.setAdapter(new BookAdapter(listRecent, this));
    }

    private void setupSearch() {
        binding.recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        // Search Mode = true (Dạng list text)
        searchAdapter = new BookAdapter(new ArrayList<>(), true, this);
        binding.recyclerBooks.setAdapter(searchAdapter);
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    binding.recyclerBooks.setVisibility(View.GONE);
                    binding.emptyStateSearch.getRoot().setVisibility(View.GONE);
                } else {
                    // Tìm kiếm từ Database (theo tên sách hoặc tác giả)
                    List<BookEntity> results = bookRepository.searchBooks(keyword);
                    List<Book> filtered = new ArrayList<>();
                    for (BookEntity e : results) {
                        filtered.add(Book.fromEntity(e));
                    }
                    List<BookEntity> entities = repo.searchBooks(keyword);
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
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBottomNav() {
        int activeColor = ContextCompat.getColor(this, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);
        binding.navHome.setOnClickListener(v -> updateBottomNavUi(R.id.nav_home, activeColor, inactiveColor));
        binding.navLibrary.setOnClickListener(v -> {
            updateBottomNavUi(R.id.nav_library, activeColor, inactiveColor);
            startActivity(new Intent(this, CurrentlyReadingListActivity.class));
        });
        binding.navReader.setOnClickListener(v -> {
            updateBottomNavUi(R.id.nav_reader, activeColor, inactiveColor);
            startActivity(new Intent(this, ReadingActivity.class));
        });
        updateBottomNavUi(R.id.nav_home, activeColor, inactiveColor);
    }

    /**
     * Mở ReadingActivity với bookId từ Database.
     */
    private void openReadingActivity(Book book) {
        Intent intent = new Intent(this, ReadingActivity.class);
        intent.putExtra("BOOK_ID", book.getId());
        startActivity(intent);
    }

    private void showBookDetailBottomSheet(Book book) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_book_detail, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Map views
        TextView txtTitle = bottomSheetView.findViewById(R.id.txtDetailTitle);
        TextView txtAuthor = bottomSheetView.findViewById(R.id.txtDetailAuthor);
        TextView txtPages = bottomSheetView.findViewById(R.id.txtDetailPages);
        TextView txtSummary = bottomSheetView.findViewById(R.id.txtDetailSummary);
        ImageView imgCover = bottomSheetView.findViewById(R.id.imgDetailCover);
    private void updateBottomNavUi(int activeId, int activeColor, int inactiveColor) {
        binding.iconHome.setColorFilter(activeId == R.id.nav_home ? activeColor : inactiveColor);
        binding.textHome.setTextColor(activeId == R.id.nav_home ? activeColor : inactiveColor);
        binding.navHome.setBackgroundResource(activeId == R.id.nav_home ? R.drawable.bottom_nav_active_bg : 0);

        binding.iconLibrary.setColorFilter(activeId == R.id.nav_library ? activeColor : inactiveColor);
        binding.textLibrary.setTextColor(activeId == R.id.nav_library ? activeColor : inactiveColor);
        binding.navLibrary.setBackgroundResource(activeId == R.id.nav_library ? R.drawable.bottom_nav_active_bg : 0);

        // Nút "Đọc sách" — mở ReadingActivity với bookId
        View btnRead = bottomSheetView.findViewById(R.id.btnDownload);
        if (btnRead != null) {
            btnRead.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                openReadingActivity(book);
            });
        }

        // Nút "Yêu thích" — toggle favourite trong DB qua FavouriteManager
        View btnFav = bottomSheetView.findViewById(R.id.btnFavorite);
        if (btnFav != null) {
            com.example.bookspace.FavouriteManager favManager =
                    new com.example.bookspace.FavouriteManager(this);
            // Cập nhật icon ban đầu
            ImageView iconFav = btnFav.findViewById(android.R.id.icon);
            // CardView chứa ImageView bên trong — tìm ImageView con
            if (btnFav instanceof androidx.cardview.widget.CardView) {
                ImageView favIcon = (ImageView) ((androidx.cardview.widget.CardView) btnFav).getChildAt(0);
                if (favIcon != null) {
                    favIcon.setImageResource(
                            favManager.isFavourite(book.getId()) ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
                    );
                }
            }

            btnFav.setOnClickListener(v -> {
                favManager.toggleFavourite(book.getId());
                // Cập nhật icon sau khi toggle (delay nhỏ để DB kịp xử lý)
                v.postDelayed(() -> {
                    if (btnFav instanceof androidx.cardview.widget.CardView) {
                        ImageView favIcon = (ImageView) ((androidx.cardview.widget.CardView) btnFav).getChildAt(0);
                        if (favIcon != null) {
                            favIcon.setImageResource(
                                    favManager.isFavourite(book.getId()) ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
                            );
                        }
                    }
                }, 300);
            });
        }

        bottomSheetDialog.setOnShowListener(dialogInterface -> {
            com.google.android.material.bottomsheet.BottomSheetDialog dialog = (com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            }
        });

        bottomSheetDialog.show();
        binding.iconReader.setColorFilter(activeId == R.id.nav_reader ? activeColor : inactiveColor);
        binding.textReader.setTextColor(activeId == R.id.nav_reader ? activeColor : inactiveColor);
        binding.navReader.setBackgroundResource(activeId == R.id.nav_reader ? R.drawable.bottom_nav_active_bg : 0);
    }

    @Override public void onBookClick(Book book) { BookDetailBottomSheet.show(this, book); }
}
