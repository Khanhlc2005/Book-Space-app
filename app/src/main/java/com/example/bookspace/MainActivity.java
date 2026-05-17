package com.example.bookspace;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.repository.BookRepository;

public class MainActivity extends AppCompatActivity implements OnBookClickListener {
    private ActivityMainBinding binding;
    private BookRepository repo;
    private static final String TAG = "MainActivity";
    private BookRepository bookRepository;
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = binding.vpFeaturedBooks.getCurrentItem();
            int totalItems = binding.vpFeaturedBooks.getAdapter() != null ? binding.vpFeaturedBooks.getAdapter().getItemCount() : 0;
            if (totalItems > 0) {
                binding.vpFeaturedBooks.setCurrentItem((currentItem + 1) % totalItems, true);
            }
            sliderHandler.postDelayed(this, 3000);
        }
    };
    private BookAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setupChips();
        setupBottomNav();
        setupRecyclerViews();
        setupSearch();
        seedDatabase();
    }

    private void seedDatabase() {
        com.example.bookspace.repository.BookRepository repo = new com.example.bookspace.repository.BookRepository(this);
        if (repo.getAllBooks().size() < 10) { // Nếu db chưa có nhiều sách
            com.example.bookspace.database.AppDatabase.databaseWriteExecutor.execute(() -> {
                com.example.bookspace.database.dao.BookDao dao = com.example.bookspace.database.AppDatabase.getInstance(this).bookDao();
                java.util.List<com.example.bookspace.database.entity.BookEntity> sampleBooks = new java.util.ArrayList<>();
                
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
                
                dao.insertAll(sampleBooks);
            });
        }
    }

    private com.example.bookspace.database.entity.BookEntity createBookEntity(String title, String author, String cover, int pages, String desc, String cat) {
        com.example.bookspace.database.entity.BookEntity b = new com.example.bookspace.database.entity.BookEntity();
        b.title = title;
        b.author = author;
        b.coverUrl = cover;
        b.pages = pages;
        b.description = desc;
        b.category = cat;
        b.isDownloaded = false;
        return b;
    }

    /**
     * Khởi động lại handler tự động trượt trang sách nổi bật khi Activity quay lại màn hình chính.
     */
    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    /**
     * Tạm dừng handler tự động trượt trang sách để tiết kiệm tài nguyên khi Activity không hiển thị.
     */
    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    /**
     * Thiết lập các thành phần giao diện tĩnh lẻ tẻ, ví dụ như tải ảnh đại diện người dùng.
     */
    private void setupStaticUI() {
        // Ảnh profile & featured cards
        String urlProfile = "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP";
        Glide.with(this).load(urlProfile).circleCrop().into(binding.imgProfile);
    }

    /**
     * Khởi tạo danh sách đối tượng sách nổi bật và gắn vào Adapter của ViewPager2.
     * Cấu hình thêm khoảng cách giữa các trang (MarginPageTransformer) cho ViewPager2.
     */
    private void setupFeaturedViewPager() {
        // Lấy tất cả sách từ DB để hiển thị trên Featured slider
        List<BookEntity> entities = bookRepository.getAllBooks();
        List<Book> listFeatured = new ArrayList<>();
        for (BookEntity e : entities) {
            Book book = new Book(e.coverUrl, e.title, e.author, e.pages, e.description, e.category);
            book.setId(e.id);
            listFeatured.add(book);
        }

        FeaturedBookAdapter adapter = new FeaturedBookAdapter(listFeatured, this);
        binding.vpFeaturedBooks.setAdapter(adapter);

        binding.vpFeaturedBooks.setOffscreenPageLimit(3);
        binding.vpFeaturedBooks.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        binding.vpFeaturedBooks.setPageTransformer(transformer);
    }

    /**
     * Khởi tạo và liên kết dữ liệu danh sách cho "Sách mới cập nhật" và "Tiểu thuyết"
     * vào các RecyclerView tương ứng chạy trượt ngang.
     */
    private void setupRecyclerViews() {
        // Lấy tất cả sách từ DB hiển thị ở "Mới cập nhật"
        List<BookEntity> allEntities = bookRepository.getAllBooks();
        List<Book> listRecent = new ArrayList<>();
        for (BookEntity e : allEntities) {
            Book book = new Book(e.coverUrl, e.title, e.author, e.pages, e.description, e.category);
            book.setId(e.id);
            listRecent.add(book);
        }

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

        // Khởi tạo mục danh mục mặc định
        filterBooksByCategory("Tất cả");
    }

    private void filterBooksByCategory(String category) {
        List<BookEntity> entities;
        if (category.equalsIgnoreCase("Tất cả")) {
            entities = repo.getAllBooks();
            binding.txtSectionCategory.setText("Tất cả");
        } else {
            entities = repo.getByCategory(category.toUpperCase());
            binding.txtSectionCategory.setText(category);
        }

        List<Book> listCategory = new ArrayList<>();
        for (BookEntity entity : entities) {
            Book book = new Book(entity.coverUrl, entity.title, entity.author, entity.pages, entity.description, entity.category);
            book.setId(entity.id);
            listCategory.add(book);
        }

        binding.rvNovels.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvNovels.setAdapter(new BookAdapter(listCategory, this));
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
                    searchAdapter.updateData(filtered);
                    
                    if (filtered.isEmpty()) {
                        binding.recyclerBooks.setVisibility(View.GONE);
                        binding.emptyStateSearch.getRoot().setVisibility(View.VISIBLE);
                        binding.emptyStateSearch.imgEmptyIcon.setImageResource(android.R.drawable.ic_menu_search);
                        binding.emptyStateSearch.txtEmptyMessage.setText("Không tìm thấy kết quả cho '" + keyword + "'");
                    } else {
                        binding.recyclerBooks.setVisibility(View.VISIBLE);
                        binding.emptyStateSearch.getRoot().setVisibility(View.GONE);
                    }
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChips() {
        LinearLayout chipContainer = binding.chipContainer;
        int activeTextColor = ContextCompat.getColor(this, R.color.on_primary);
        int inactiveTextColor = ContextCompat.getColor(this, R.color.on_surface_variant);

        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            View child = chipContainer.getChildAt(i);
            if (child instanceof TextView) {
                child.setOnClickListener(v -> {
                    for (int j = 0; j < chipContainer.getChildCount(); j++) {
                        View otherChip = chipContainer.getChildAt(j);
                        if (otherChip instanceof TextView) {
                            otherChip.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_bg));
                            ((TextView) otherChip).setTextColor(inactiveTextColor);
                        }
                    }
                    v.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_active_bg));
                    ((TextView) v).setTextColor(activeTextColor);

                    String category = ((TextView) v).getText().toString();
                    filterBooksByCategory(category);
                    
                    if (binding.mainScrollView != null && binding.txtSectionCategory != null) {
                        binding.mainScrollView.post(() -> {
                            binding.mainScrollView.smoothScrollTo(0, binding.txtSectionCategory.getTop());
                        });
                    }
                });
            }
        }
    }

    private void setupBottomNav() {
        int[][] navSets = {
                {R.id.nav_home, R.id.icon_home, R.id.text_home},
                {R.id.nav_library, R.id.icon_library, R.id.text_library},
                {R.id.nav_reader, R.id.icon_reader, R.id.text_reader}
        };

        int activeColor = ContextCompat.getColor(this, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        for (int[] ids : navSets) {
            View container = findViewById(ids[0]);
            container.setOnClickListener(v -> {
                if (ids[0] == R.id.nav_reader) {
                    startActivity(new Intent(this, ReadingActivity.class));
                } else if (ids[0] == R.id.nav_library) {
                    startActivity(new Intent(this, CurrentlyReadingListActivity.class));
                }
                for (int[] other : navSets) {
                    View otherContainer = findViewById(other[0]);
                    otherContainer.setBackground(null);
                    ((ImageView) otherContainer.findViewById(other[1])).setColorFilter(inactiveColor);
                    ((TextView) otherContainer.findViewById(other[2])).setTextColor(inactiveColor);
                }
                v.setBackground(ContextCompat.getDrawable(this, R.drawable.bottom_nav_active_bg));
                ((ImageView) v.findViewById(ids[1])).setColorFilter(activeColor);
                ((TextView) v.findViewById(ids[2])).setTextColor(activeColor);
            });
        }
    }

    @Override
    public void onBookClick(Book book) {
        BookDetailBottomSheet.show(this, book);
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

        // Set data
        if (txtTitle != null) txtTitle.setText(book.getTitle());
        if (txtAuthor != null) txtAuthor.setText("Tác giả: " + book.getAuthor());
        if (txtPages != null) txtPages.setText(String.valueOf(book.getPages()));
        
        if (txtSummary != null) {
            if (book.getDescription() != null && !book.getDescription().isEmpty()) {
                txtSummary.setText(book.getDescription());
            } else {
                txtSummary.setText("Chưa có tóm tắt cho cuốn sách này.");
            }
        }

        if (imgCover != null && book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(this)
                 .load(book.getCoverUrl())
                 .transform(new CenterCrop(), new RoundedCorners(24))
                 .into(imgCover);
        }

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
    }
}
