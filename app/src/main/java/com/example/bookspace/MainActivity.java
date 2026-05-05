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
import androidx.annotation.NonNull;
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
import com.example.bookspace.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

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
        setupDrawerMenu();
        setupBottomNav();
        setupRecyclerViews();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
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
        
        // Nút mở Menu
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
        List<Book> listFeatured = new ArrayList<>();
        listFeatured.add(new Book("https://picsum.photos/600/400?random=101", "Trưởng Thành Sau Ngàn Lần Tranh Đấu", "Rando Kim", 300, "", "KỸ NĂNG SỐNG"));
        listFeatured.add(new Book("https://picsum.photos/600/400?random=102", "Một Thoáng Ta Rực Rỡ Ở Nhân Gian", "Ocean Vuong", 350, "", "TIỂU THUYẾT"));
        listFeatured.add(new Book("https://picsum.photos/600/400?random=103", "Thiên Tài Bên Trái, Kẻ Điên Bên Phải", "Cao Minh", 400, "", "TÂM LÝ HỌC"));
        FeaturedBookAdapter adapter = new FeaturedBookAdapter(listFeatured, this);
        binding.vpFeaturedBooks.setAdapter(adapter);
        binding.vpFeaturedBooks.setOffscreenPageLimit(3);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        binding.vpFeaturedBooks.setPageTransformer(transformer);
    }

    private void setupRecyclerViews() {
        List<Book> listRecent = new ArrayList<>();
        listRecent.add(new Book("https://picsum.photos/200/300?random=11", "Đắc Nhân Tâm", "Dale Carnegie", 320, "Sách kỹ năng sống hay nhất...", "KỸ NĂNG SỐNG"));
        listRecent.add(new Book("https://picsum.photos/200/300?random=12", "Nhà Giả Kim", "Paulo Coelho", 200, "Hành trình tìm kiếm vận mệnh...", "TIỂU THUYẾT"));

        binding.rvRecentlyUpdated.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvRecentlyUpdated.setAdapter(new BookAdapter(listRecent, this));

        List<Book> listNovel = new ArrayList<>();
        listNovel.add(new Book("https://picsum.photos/200/300?random=21", "Harry Potter", "J.K. Rowling", 500, "Thế giới phù thủy kỳ bí...", "TIỂU THUYẾT"));
        binding.rvNovels.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvNovels.setAdapter(new BookAdapter(listNovel, this));
    }

    private void setupSearch() {
        allBooksForSearch = new ArrayList<>();
        allBooksForSearch.add(new Book("https://picsum.photos/200/300?random=4", "Harry Potter", "J.K. Rowling", 500, "Phù thủy"));
        binding.recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
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

    private void setupBottomNav() {
        int activeColor = ContextCompat.getColor(this, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        binding.navHome.setOnClickListener(v -> updateBottomNav(R.id.nav_home));
        binding.navLibrary.setOnClickListener(v -> {
            updateBottomNav(R.id.nav_library);
            startActivity(new Intent(this, CurrentlyReadingListActivity.class));
        });
        binding.navReader.setOnClickListener(v -> {
            updateBottomNav(R.id.nav_reader);
            startActivity(new Intent(this, ReadingActivity.class));
        });
    }

    private void updateBottomNav(int activeId) {
        int activeColor = ContextCompat.getColor(this, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        binding.navHome.setBackground(activeId == R.id.nav_home ? ContextCompat.getDrawable(this, R.drawable.bottom_nav_active_bg) : null);
        binding.iconHome.setColorFilter(activeId == R.id.nav_home ? activeColor : inactiveColor);
        binding.textHome.setTextColor(activeId == R.id.nav_home ? activeColor : inactiveColor);

        binding.navLibrary.setBackground(activeId == R.id.nav_library ? ContextCompat.getDrawable(this, R.drawable.bottom_nav_active_bg) : null);
        binding.iconLibrary.setColorFilter(activeId == R.id.nav_library ? activeColor : inactiveColor);
        binding.textLibrary.setTextColor(activeId == R.id.nav_library ? activeColor : inactiveColor);

        binding.navReader.setBackground(activeId == R.id.nav_reader ? ContextCompat.getDrawable(this, R.drawable.bottom_nav_active_bg) : null);
        binding.iconReader.setColorFilter(activeId == R.id.nav_reader ? activeColor : inactiveColor);
        binding.textReader.setTextColor(activeId == R.id.nav_reader ? activeColor : inactiveColor);
    }

    @Override
    public void onBookClick(Book book) {
        showBookDetailBottomSheet(book);
    }

    private void showBookDetailBottomSheet(Book book) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_book_detail, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView txtTitle = bottomSheetView.findViewById(R.id.txtDetailTitle);
        TextView txtAuthor = bottomSheetView.findViewById(R.id.txtDetailAuthor);
        ImageView imgCover = bottomSheetView.findViewById(R.id.imgDetailCover);

        if (txtTitle != null) txtTitle.setText(book.getTitle());
        if (txtAuthor != null) txtAuthor.setText("Tác giả: " + book.getAuthor());
        if (imgCover != null) Glide.with(this).load(book.getCoverUrl()).into(imgCover);

        bottomSheetDialog.show();

        // Nút "Đọc ngay" — truyền bookId sang ReadingActivity
        bottomSheetView.findViewById(R.id.btnDownload).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(MainActivity.this, ReadingActivity.class);
            intent.putExtra("bookId", book.getId());
            startActivity(intent);
        });
    }
}
