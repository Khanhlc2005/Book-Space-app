package com.example.bookspace;

import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.bookspace.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ArrayList<Book> allBooksForSearch = new ArrayList<>();
    private BookAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setupChips();
        setupBottomNav();
        setupRecyclerViews();
        setupSearch();
    }

    private void setupStaticUI() {
        // Ảnh profile & featured cards
        String urlProfile = "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP";
        Glide.with(this).load(urlProfile).circleCrop().into(binding.imgProfile);
        
        // Load ảnh cho Featured 1 & 2 (Dùng tạm URL mẫu)
        Glide.with(this).load("https://picsum.photos/600/400?random=1").centerCrop().into(binding.imgFeatured1);
        Glide.with(this).load("https://picsum.photos/600/400?random=2").centerCrop().into(binding.imgFeatured2);
    }

    private void setupRecyclerViews() {
        // Dữ liệu mẫu (Số nhiều và linh hoạt hơn)
        List<Book> listRecent = new ArrayList<>();
        listRecent.add(new Book("https://picsum.photos/200/300?random=11", "Đắc Nhân Tâm", "Dale Carnegie", 320, "Sách kỹ năng sống hay nhất..."));
        listRecent.add(new Book("https://picsum.photos/200/300?random=12", "Nhà Giả Kim", "Paulo Coelho", 200, "Hành trình tìm kiếm vận mệnh..."));
        listRecent.add(new Book("https://picsum.photos/200/300?random=13", "Muôn kiếp nhân sinh", "Nguyên Phong", 450, "Luân hồi và nhân quả..."));

        List<Book> listNovel = new ArrayList<>();
        listNovel.add(new Book("https://picsum.photos/200/300?random=21", "Harry Potter", "J.K. Rowling", 500, "Thế giới phù thủy kỳ bí..."));
        listNovel.add(new Book("https://picsum.photos/200/300?random=22", "Chúa tể nhẫn", "J.R.R. Tolkien", 1200, "Cuộc chiến giành chiếc nhẫn..."));

        binding.rvRecentlyUpdated.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvRecentlyUpdated.setAdapter(new BookAdapter(listRecent));

        binding.rvNovels.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvNovels.setAdapter(new BookAdapter(listNovel));
    }

    private void setupSearch() {
        allBooksForSearch = new ArrayList<>();
        allBooksForSearch.add(new Book("https://picsum.photos/200/300?random=4", "Harry Potter", "J.K. Rowling", 500, "Phù thủy"));
        allBooksForSearch.add(new Book("https://picsum.photos/200/300?random=5", "Sherlock Holmes", "Conan Doyle", 600, "Trinh thám"));
        allBooksForSearch.add(new Book("https://picsum.photos/200/300?random=6", "Doraemon", "Fujiko Fujio", 100, "Truyện tranh"));

        binding.recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        // Search Mode = true (Dạng list text như lúc trước)
        searchAdapter = new BookAdapter(new ArrayList<>(), true);
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
}
