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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.bookspace.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Book> bookList = new ArrayList<>();
    private ActivityMainBinding binding;

    private List<Book> filteredList;
    private BookAdapter adapter;


    // Book cover image URLs (from Stitch design)
    private static final String URL_FEATURED_1 =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuBqg1J6P6UJqOunvW8Y70nRZ2ivb6jvSXaR15Po9UMZiBBl-j9JZAt0luYOsoUfi1EA7mKsuzy5YVfRGMxSZKEH1XAMvHeXmMmA_wbCJNb6DiK44q0d7Q9DpX6XiFSRmsFTHcfjOrLF5bULQ8-65FkzkaaUVUZpevLifgYyF5n0Jxis6L8O-ATC9mXBYDrhrWLI-zgofVbMlu9KCmqfWcGbrn2Cm4Cm7JnP-2GfycqP2ky2H-se-wanhviHCJiB-AET0IQqtKG1QRyb";

    private static final String URL_FEATURED_2 =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuA-2c0Pienj4-mNNwqQG2mmQ0O6F_wr6QA0OOggON4iheQnTZd1ENvBTtJa97wJ0NuMvfr0yTnTe1tEvtSlqup3Qy3QKZgkl-vgLXWllK9RyHKRd7c-31pLnF-yq0Y88UiIzh3V94A92_TW1BX_r9UTZ8NyYgfb9eKhnRL1SW2hn9YdLpyBv_efqKZjakuKPHiy8C8ZMWJpPnbxryw5J0GVE9LmqcF7zlx_uAM-iAPhu4MgVYaa3EE_oYzdI-BL1Y_dK894BbcZ-cRB";

    private static final String URL_BOOK_1 =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuB5YGPSjxC2jR7_EZjLhMP4FxqBDlIQ-1YdJ01UScnlnj8aqveHHCBg6yk8zvf-fE81Y_KUDmm8Qi4zvfLrbqjE-qzK6bx07CJjb3JRVa-laQOf852LnUtFiIHpb0DKb4tD03IEfIELb3UgfIshp1sE2QTQwU8cnKO9WyOxfkFxRDLNoYxQm9BFq-PQ8Pp87Fu09jhBsfRJRQoktRbJ57vCkKfFjMcNT6ONCKsdCz0TCC-baA31Dzo_20PNSzwJN0WCH3lngBIOtC8e";

    private static final String URL_BOOK_2 =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuAWy_KmcuwtA9T_j08zde0NMyeMSR76StO9V1aCrsxtyZTR3UileI49aztWJ2Abb0KW9OCjaIifC8eelO4WMsyJbykXUOj62Zco0n7AkwojI_Ty16Prvg9l87r3w7_KKteD5mSwxGNCxOmFl1NsdRMTLcWqe4Rq4xq8SJteX1EGeu6QkwnO-1uMsr6lfUTQGjnmu_ZmSCazPq8YZ_DsbzfwM2rMd3V3TcMdJqc6LqUihOpbOFVOaZWx5ZSg8ubrBcxayk4Q2opayml6";

    private static final String URL_BOOK_3 =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuBh916BRPCGqGuQ1rxfa3oG25jWT3GS5fOoa_PS46jkepHPgC2V6P5VUQo3XD8OzCKl05nzpE07cKlFFL3mTNDGZ0Y_ewZTD7ipeQMncVBtF7sic9Myo-EX93Oq3an6e1DF5uAMLVrTb2w6X2L1CI7xEcRHvM-_kW4OEnbnmeh0-mspMvZOnq9a4Ss28kg_h4oJgsDLeAWK1AU8gKiC0JLrSIlvS9ieE73jFbCFHDBjCzMQoGV2E_E5JYLX8bTlTntPGOa0m19xU5Pd";

    private static final String URL_BOOK_4 =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCjUpNffG38zrDBBO-5Uwe6HrlSL38zWHZaCfP2jbry3s3EKdMxrxOHHgANx1tQCWGbagJ8gSaRF4E5bV0bYM2hYFt2gGH59GAhT1z0c2YV6Yz3S-XSGAnb-_8MqnOHRk1ruk2MAPguAyAyNlCmaJeKuMCzGVhYXld09mNEIhNm2kK4T9gCGIUQxTL9T59lYZv5EO2jB6_SJjEXha_Ys-BVYw4jObzV6F0yUuvo-zhdMDOq8rsuDZGEWeuPBor3iOstdkjzeKk6tuTp";

    private static final String URL_PROFILE =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBar.setPadding(
                    binding.topBar.getPaddingLeft(),
                    insets.top + 16,
                    binding.topBar.getPaddingRight(),
                    binding.topBar.getPaddingBottom()
            );
            binding.bottomNavContainer.setPadding(
                    binding.bottomNavContainer.getPaddingLeft(),
                    binding.bottomNavContainer.getPaddingTop(),
                    binding.bottomNavContainer.getPaddingRight(),
                    insets.bottom + 24
            );
            return WindowInsetsCompat.CONSUMED;
        });


        loadImages();
        setupChips();
        setupBottomNav();

        //  THÊM SEARCH
        setupSearch();

    }

    //  SEARCH LOGIC
    private void setupSearch() {
        bookList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // DATA TEST
        bookList.add(new Book("Harry Potter", "J.K. Rowling"));
        bookList.add(new Book("Sherlock Holmes", "Arthur Conan Doyle"));
        bookList.add(new Book("Doraemon", "Fujiko Fujio"));
        bookList.add(new Book("Clean Code", "Robert C. Martin"));


        // ===== RECYCLER VIEW =====
        binding.recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo adapter với chế độ searchMode = true
        adapter = new BookAdapter(filteredList, true);
        binding.recyclerBooks.setAdapter(adapter);

        // ===== SEARCH =====
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String keyword = s.toString().trim();

                //  Nếu chưa nhập gì → không hiện gì
                if (keyword.isEmpty()) {
                    filteredList.clear();
                    adapter.updateData(filteredList);
                    binding.recyclerBooks.setVisibility(View.GONE);
                    return;
                }

                // Có nhập → mới search
                filteredList = Searchbar.filter(bookList, keyword);
                adapter.updateData(filteredList);
                binding.recyclerBooks.setVisibility(View.VISIBLE);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }


    private void loadImages() {
        Glide.with(this).load(URL_PROFILE).circleCrop().into(binding.imgProfile);
        Glide.with(this).load(URL_FEATURED_1).centerCrop().into(binding.imgFeatured1);
        Glide.with(this).load(URL_FEATURED_2).centerCrop().into(binding.imgFeatured2);

        Glide.with(this).load(URL_BOOK_1)
                .transform(new CenterCrop(), new RoundedCorners(24))
                .into(binding.imgBook1);

        Glide.with(this).load(URL_BOOK_2)
                .transform(new CenterCrop(), new RoundedCorners(24))
                .into(binding.imgBook2);

        Glide.with(this).load(URL_BOOK_3)
                .transform(new CenterCrop(), new RoundedCorners(24))
                .into(binding.imgBook3);

        Glide.with(this).load(URL_BOOK_4)
                .transform(new CenterCrop(), new RoundedCorners(24))
                .into(binding.imgBook4);
    }

    private void setupChips() {
        LinearLayout chipContainer = binding.chipContainer;
        int chipCount = chipContainer.getChildCount();

        int activeTextColor = ContextCompat.getColor(this, R.color.on_primary);
        int inactiveTextColor = ContextCompat.getColor(this, R.color.on_surface_variant);

        for (int i = 0; i < chipCount; i++) {
            View child = chipContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                chip.setOnClickListener(v -> {
                    for (int j = 0; j < chipCount; j++) {
                        View other = chipContainer.getChildAt(j);
                        if (other instanceof TextView) {
                            other.setBackground(
                                    ContextCompat.getDrawable(this, R.drawable.chip_bg));
                            ((TextView) other).setTextColor(inactiveTextColor);
                        }
                    }
                    v.setBackground(
                            ContextCompat.getDrawable(this, R.drawable.chip_active_bg));
                    ((TextView) v).setTextColor(activeTextColor);

                    String category = chip.getText().toString();
                    if (category.equals(getString(R.string.cat_life_skills)) || 
                        category.equals(getString(R.string.cat_psychology))) {
                        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                        intent.putExtra("CATEGORY_NAME", category);
                        startActivity(intent);
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
            View container = binding.getRoot().findViewById(ids[0]);
            container.setOnClickListener(v -> {
                for (int[] other : navSets) {
                    View otherContainer = binding.getRoot().findViewById(other[0]);
                    otherContainer.setBackground(null);
                    ((ImageView) otherContainer.findViewById(other[1]))
                            .setColorFilter(inactiveColor);
                    ((TextView) otherContainer.findViewById(other[2]))
                            .setTextColor(inactiveColor);
                }
                v.setBackground(
                        ContextCompat.getDrawable(this, R.drawable.bottom_nav_active_bg));
                ((ImageView) v.findViewById(ids[1]))
                        .setColorFilter(activeColor);
                ((TextView) v.findViewById(ids[2]))
                        .setTextColor(activeColor);
            });
        }
    }
}