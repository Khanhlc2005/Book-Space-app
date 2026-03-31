package com.example.bookspace;

import android.os.Bundle;
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

    // Các URL ảnh cho phần Featured
    private static final String URL_PROFILE = "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP";
    private static final String URL_FEATURED_1 = "https://lh3.googleusercontent.com/aida-public/AB6AXuBqg1J6P6UJqOunvW8Y70nRZ2ivb6jvSXaR15Po9UMZiBBl-j9JZAt0luYOsoUfi1EA7mKsuzy5YVfRGMxSZKEH1XAMvHeXmMmA_wbCJNb6DiK44q0d7Q9DpX6XiFSRmsFTHcfjOrLF5bULQ8-65FkzkaaUVUZpevLifgYyF5n0Jxis6L8O-ATC9mXBYDrhrWLI-zgofVbMlu9KCmqfWcGbrn2Cm4Cm7JnP-2GfycqP2ky2H-se-wanhviHCJiB-AET0IQqtKG1QRyb";
    private static final String URL_FEATURED_2 = "https://lh3.googleusercontent.com/aida-public/AB6AXuA-2c0Pienj4-mNNwqQG2mmQ0O6F_wr6QA0OOggON4iheQnTZd1ENvBTtJa97wJ0NuMvfr0yTnTe1tEvtSlqup3Qy3QKZgkl-vgLXWllK9RyHKRd7c-31pLnF-yq0Y88UiIzh3V94A92_TW1BX_r9UTZ8NyYgfb9eKhnRL1SW2hn9YdLpyBv_efqKZjakuKPHiy8C8ZMWJpPnbxryw5J0GVE9LmqcF7zlx_uAM-iAPhu4MgVYaa3EE_oYzdI-BL1Y_dK894BbcZ-cRB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý System Bar Insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBar.setPadding(binding.topBar.getPaddingLeft(), insets.top + 16, binding.topBar.getPaddingRight(), binding.topBar.getPaddingBottom());
            binding.bottomNavContainer.setPadding(binding.bottomNavContainer.getPaddingLeft(), binding.bottomNavContainer.getPaddingTop(), binding.bottomNavContainer.getPaddingRight(), insets.bottom + 24);
            return WindowInsetsCompat.CONSUMED;
        });

        loadStaticImages();
        setupChips();
        setupBottomNav();
        setupRecyclerViews();
    }

    private void loadStaticImages() {
        Glide.with(this).load(URL_PROFILE).circleCrop().into(binding.imgProfile);
        Glide.with(this).load(URL_FEATURED_1).centerCrop().into(binding.imgFeatured1);
        Glide.with(this).load(URL_FEATURED_2).centerCrop().into(binding.imgFeatured2);
    }

    private void setupRecyclerViews() {
        // Dữ liệu mồi cho phần "Mới cập nhật"
        List<Book> listRecent = new ArrayList<>();
        listRecent.add(new Book(R.drawable.book_cover_bg, "Đắc Nhân Tâm", "Dale Carnegie"));
        listRecent.add(new Book(R.drawable.book_cover_bg, "Nhà Giả Kim", "Paulo Coelho"));
        listRecent.add(new Book(R.drawable.book_cover_bg, "Muôn kiếp nhân sinh", "Nguyên Phong"));

        // Dữ liệu mồi cho phần "Tiểu thuyết"
        List<Book> listNovel = new ArrayList<>();
        listNovel.add(new Book(R.drawable.book_cover_bg, "Harry Potter", "J.K. Rowling"));
        listNovel.add(new Book(R.drawable.book_cover_bg, "Chúa tể những chiếc nhẫn", "J.R.R. Tolkien"));
        listNovel.add(new Book(R.drawable.book_cover_bg, "Sherlock Holmes", "Arthur Conan Doyle"));

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
            View container = findViewById(ids[0]);
            container.setOnClickListener(v -> {
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