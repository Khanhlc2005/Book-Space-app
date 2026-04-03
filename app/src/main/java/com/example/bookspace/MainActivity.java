package com.example.bookspace;

import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "MainActivity";

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
            try {
                View container = findViewById(ids[0]);
                if (container != null) {
                    container.setOnClickListener(v -> {
                        try {
                            for (int[] other : navSets) {
                                View otherContainer = findViewById(other[0]);
                                if (otherContainer != null) {
                                    otherContainer.setBackground(null);
                                    ImageView icon = otherContainer.findViewById(other[1]);
                                    TextView text = otherContainer.findViewById(other[2]);
                                    if (icon != null) icon.setColorFilter(inactiveColor);
                                    if (text != null) text.setTextColor(inactiveColor);
                                }
                            }
                            v.setBackground(ContextCompat.getDrawable(this, R.drawable.bottom_nav_active_bg));
                            ImageView activeIcon = v.findViewById(ids[1]);
                            TextView activeText = v.findViewById(ids[2]);
                            if (activeIcon != null) activeIcon.setColorFilter(activeColor);
                            if (activeText != null) activeText.setTextColor(activeColor);
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi xử lý click BottomNav: " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi setup BottomNav ID " + ids[0] + ": " + e.getMessage());
            }
        }
    }
}
