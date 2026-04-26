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
import com.example.bookspace.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnBookClickListener {
    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null) {
                return;
            }
            int currentItem = binding.vpFeaturedBooks.getCurrentItem();
            int totalItems = binding.vpFeaturedBooks.getAdapter() != null ? binding.vpFeaturedBooks.getAdapter().getItemCount() : 0;
            if (totalItems > 0) {
                binding.vpFeaturedBooks.setCurrentItem((currentItem + 1) % totalItems, true);
            }
            sliderHandler.postDelayed(this, 3000);
        }
    };
    private ArrayList<Book> allBooksForSearch = new ArrayList<>();
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

        // System Bar Insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBar.setPadding(binding.topBar.getPaddingLeft(), insets.top + 16, binding.topBar.getPaddingRight(), binding.topBar.getPaddingBottom());
            binding.bottomNavContainer.setPadding(binding.bottomNavContainer.getPaddingLeft(), binding.bottomNavContainer.getPaddingTop(), binding.bottomNavContainer.getPaddingRight(), insets.bottom + 24);
            return WindowInsetsCompat.CONSUMED;
        });

        setupStaticUI();
        setupFeaturedViewPager();
        setupChips();
        setupBottomNav();
        setupRecyclerViews();
        setupSearch();
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
        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    /**
     * Khởi tạo danh sách đối tượng sách nổi bật và gắn vào Adapter của ViewPager2.
     * Cấu hình thêm khoảng cách giữa các trang (MarginPageTransformer) cho ViewPager2.
     */
    private void setupFeaturedViewPager() {
        List<Book> listFeatured = new ArrayList<>();
        listFeatured.add(new Book("https://picsum.photos/600/400?random=101", "Trưởng Thành Sau Ngàn Lần Tranh Đấu", "Rando Kim", 300, "", "KỸ NĂNG SỐNG"));
        listFeatured.add(new Book("https://picsum.photos/600/400?random=102", "Một Thoáng Ta Rực Rỡ Ở Nhân Gian", "Ocean Vuong", 350, "", "TIỂU THUYẾT"));
        listFeatured.add(new Book("https://picsum.photos/600/400?random=103", "Thiên Tài Bên Trái, Kẻ Điên Bên Phải", "Cao Minh", 400, "", "TÂM LÝ HỌC"));
        listFeatured.add(new Book("https://picsum.photos/600/400?random=104", "Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", 250, "", "KỸ NĂNG SỐNG"));
        listFeatured.add(new Book("https://picsum.photos/600/400?random=105", "Dám Bị Ghét", "Kishimi Ichiro", 320, "", "TÂM LÝ"));

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
        // Dữ liệu mẫu (Số nhiều và linh hoạt hơn)
        List<Book> listRecent = new ArrayList<>();
        listRecent.add(new Book("https://picsum.photos/200/300?random=11", "Đắc Nhân Tâm", "Dale Carnegie", 320, "Sách kỹ năng sống hay nhất...", "KỸ NĂNG SỐNG"));
        listRecent.add(new Book("https://picsum.photos/200/300?random=12", "Nhà Giả Kim", "Paulo Coelho", 200, "Hành trình tìm kiếm vận mệnh...", "TIỂU THUYẾT"));
        listRecent.add(new Book("https://picsum.photos/200/300?random=13", "Muôn kiếp nhân sinh", "Nguyên Phong", 450, "Luân hồi và nhân quả...", "TÂM LINH"));

        List<Book> listNovel = new ArrayList<>();
        listNovel.add(new Book("https://picsum.photos/200/300?random=21", "Harry Potter", "J.K. Rowling", 500, "Thế giới phù thủy kỳ bí...", "TIỂU THUYẾT"));
        listNovel.add(new Book("https://picsum.photos/200/300?random=22", "Chúa tể nhẫn", "J.R.R. Tolkien", 1200, "Cuộc chiến giành chiếc nhẫn...", "TIỂU THUYẾT"));

        binding.rvRecentlyUpdated.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvRecentlyUpdated.setAdapter(new BookAdapter(listRecent, this));

        binding.rvNovels.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvNovels.setAdapter(new BookAdapter(listNovel, this));
    }

    private void setupSearch() {
        allBooksForSearch = new ArrayList<>();
        allBooksForSearch.add(new Book("https://picsum.photos/200/300?random=4", "Harry Potter", "J.K. Rowling", 500, "Phù thủy"));
        allBooksForSearch.add(new Book("https://picsum.photos/200/300?random=5", "Sherlock Holmes", "Conan Doyle", 600, "Trinh thám"));
        allBooksForSearch.add(new Book("https://picsum.photos/200/300?random=6", "Doraemon", "Fujiko Fujio", 100, "Truyện tranh"));

        binding.recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        // Search Mode = true (Dạng list text như lúc trước)
        searchAdapter = new BookAdapter(new ArrayList<>(), true, this);
        binding.recyclerBooks.setAdapter(searchAdapter);

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    binding.recyclerBooks.setVisibility(View.GONE);
                } else {
                    List<Book> filtered = new ArrayList<>();
                    for (Book b : allBooksForSearch) {
                        if (b.getTitle().toLowerCase().contains(keyword.toLowerCase())) filtered.add(b);
                    }
                    searchAdapter.updateData(filtered);
                    binding.recyclerBooks.setVisibility(View.VISIBLE);
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
        showBookDetailBottomSheet(book);
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
