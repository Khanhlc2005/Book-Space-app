package com.example.bookspace;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.bookspace.databinding.ActivityCategoryBinding;
import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryActivity extends AppCompatActivity implements OnBookClickListener {

    private ActivityCategoryBinding binding;
    private BookAdapter adapter;
    private List<Book> categoryBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý Insets để không bị đè bởi thanh hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainCategory, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topBar.setPadding(
                    binding.topBar.getPaddingLeft(),
                    insets.top + 16,
                    binding.topBar.getPaddingRight(),
                    binding.topBar.getPaddingBottom()
            );
            binding.recyclerCategoryBooks.setPadding(
                    binding.recyclerCategoryBooks.getPaddingLeft(),
                    binding.recyclerCategoryBooks.getPaddingTop(),
                    binding.recyclerCategoryBooks.getPaddingRight(),
                    insets.bottom + 16
            );
            return WindowInsetsCompat.CONSUMED;
        });

        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        binding.txtCategoryTitle.setText(categoryName);

        binding.btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadCategoryData(categoryName);
    }

    private void setupRecyclerView() {
        // Grid 2 cột giống activity_main.xml
        binding.recyclerCategoryBooks.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BookAdapter(categoryBooks, this);
        binding.recyclerCategoryBooks.setAdapter(adapter);
    }

    private void loadCategoryData(String category) {
        categoryBooks.clear();
        if (category == null) return;

        if (category.equals(getString(R.string.cat_life_skills))) {
            // Dữ liệu mẫu cho Kỹ năng sống
            categoryBooks.add(new Book("Đắc nhân tâm", "Dale Carnegie", 
                "https://lh3.googleusercontent.com/aida-public/AB6AXuBh916BRPCGqGuQ1rxfa3oG25jWT3GS5fOoa_PS46jkepHPgC2V6P5VUQo3XD8OzCKl05nzpE07cKlFFL3mTNDGZ0Y_ewZTD7ipeQMncVBtF7sic9Myo-EX93Oq3an6e1DF5uAMLVrTb2w6X2L1CI7xEcRHvM-_kW4OEnbnmeh0-mspMvZOnq9a4Ss28kg_h4oJgsDLeAWK1AU8gKiC0JLrSIlvS9ieE73jFbCFHDBjCzMQoGV2E_E5JYLX8bTlTntPGOa0m19xU5Pd"));
            
            categoryBooks.add(new Book("7 thói quen để thành đạt", "Stephen R. Covey",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuB5YGPSjxC2jR7_EZjLhMP4FxqBDlIQ-1YdJ01UScnlnj8aqveHHCBg6yk8zvf-fE81Y_KUDmm8Qi4zvfLrbqjE-qzK6bx07CJjb3JRVa-laQOf852LnUtFiIHpb0DKb4tD03IEfIELb3UgfIshp1sE2QTQwU8cnKO9WyOxfkFxRDLNoYxQm9BFq-PQ8Pp87Fu09jhBsfRJRQoktRbJ57vCkKfFjMcNT6ONCKsdCz0TCC-baA31Dzo_20PNSzwJN0WCH3lngBIOtC8e"));
            
            categoryBooks.add(new Book("Đời ngắn đừng ngủ dài", "Robin Sharma",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAWy_KmcuwtA9T_j08zde0NMyeMSR76StO9V1aCrsxtyZTR3UileI49aztWJ2Abb0KW9OCjaIifC8eelO4WMsyJbykXUOj62Zco0n7AkwojI_Ty16Prvg9l87r3w7_KKteD5mSwxGNCxOmFl1NsdRMTLcWqe4Rq4xq8SJteX1EGeu6QkwnO-1uMsr6lfUTQGjnmu_ZmSCazPq8YZ_DsbzfwM2rMd3V3TcMdJqc6LqUihOpbOFVOaZWx5ZSg8ubrBcxayk4Q2opayml6"));
            
            categoryBooks.add(new Book("Khéo ăn nói sẽ có được thiên hạ", "Trác Nhã",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuCjUpNffG38zrDBBO-5Uwe6HrlSL38zWHZaCfP2jbry3s3EKdMxrxOHHgANx1tQCWGbagJ8gSaRF4E5bV0bYM2hYFt2gGH59GAhT1z0c2YV6Yz3S-XSGAnb-_8MqnOHRk1ruk2MAPguAyAyNlCmaJeKuMCzGVhYXld09mNEIhNm2kK4T9gCGIUQxTL9T59lYZv5EO2jB6_SJjEXha_Ys-BVYw4jObzV6F0yUuvo-zhdMDOq8rsuDZGEWeuPBor3iOstdkjzeKk6tuTp"));
        } else if (category.equals(getString(R.string.cat_psychology))) {
            // Dữ liệu mẫu cho Tâm lý
            categoryBooks.add(new Book("Tâm lý học về tiền", "Morgan Housel",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuA-2c0Pienj4-mNNwqQG2mmQ0O6F_wr6QA0OOggON4iheQnTZd1ENvBTtJa97wJ0NuMvfr0yTnTe1tEvtSlqup3Qy3QKZgkl-vgLXWllK9RyHKRd7c-31pLnF-yq0Y88UiIzh3V94A92_TW1BX_r9UTZ8NyYgfb9eKhnRL1SW2hn9YdLpyBv_efqKZjakuKPHiy8C8ZMWJpPnbxryw5J0GVE9LmqcF7zlx_uAM-iAPhu4MgVYaa3EE_oYzdI-BL1Y_dK894BbcZ-cRB"));
            
            categoryBooks.add(new Book("Tư duy nhanh và chậm", "Daniel Kahneman",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuBqg1J6P6UJqOunvW8Y70nRZ2ivb6jvSXaR15Po9UMZiBBl-j9JZAt0luYOsoUfi1EA7mKsuzy5YVfRGMxSZKEH1XAMvHeXmMmA_wbCJNb6DiK44q0d7Q9DpX6XiFSRmsFTHcfjOrLF5bULQ8-65FkzkaaUVUZpevLifgYyF5n0Jxis6L8O-ATC9mXBYDrhrWLI-zgofVbMlu9KCmqfWcGbrn2Cm4Cm7JnP-2GfycqP2ky2H-se-wanhviHCJiB-AET0IQqtKG1QRyb"));

            categoryBooks.add(new Book("Đọc vị bất kỳ ai", "David J. Lieberman",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP"));

            categoryBooks.add(new Book("Sức mạnh của hiện tại", "Eckhart Tolle",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuB5YGPSjxC2jR7_EZjLhMP4FxqBDlIQ-1YdJ01UScnlnj8aqveHHCBg6yk8zvf-fE81Y_KUDmm8Qi4zvfLrbqjE-qzK6bx07CJjb3JRVa-laQOf852LnUtFiIHpb0DKb4tD03IEfIELb3UgfIshp1sE2QTQwU8cnKO9WyOxfkFxRDLNoYxQm9BFq-PQ8Pp87Fu09jhBsfRJRQoktRbJ57vCkKfFjMcNT6ONCKsdCz0TCC-baA31Dzo_20PNSzwJN0WCH3lngBIOtC8e"));
        }
        adapter.updateData(categoryBooks);
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