package com.example.bookspace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class ProfileActivity extends AppCompatActivity {
    
    public static final String PREFS_NAME = "bookspace_profile";
    public static final String KEY_DISPLAY_NAME = "display_name";
    public static final String KEY_EMAIL = "email";

    private AppBarLayout appBarLayout;
    private MaterialToolbar toolbar;
    private FloatingActionButton btnEditAvatar;
    private ConstraintLayout rowDisplayName, rowEmail;
    private LinearProgressIndicator progressBarLevel;
    private MaterialCardView rowAboutUs, rowLogout;
    
    private ImageView imgAvatar;
    private TextView txtProfileName, txtDisplayNameValue, txtEmailValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Ánh xạ View
        initViews();
        
        // Thiết lập sự kiện
        setupListeners();
        
        // Cập nhật dữ liệu động (Ví dụ ProgressBar)
        updateData();
    }

    private void initViews() {
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        rowDisplayName = findViewById(R.id.rowDisplayName);
        rowEmail = findViewById(R.id.rowEmail);
        progressBarLevel = findViewById(R.id.progressBarLevel);
        rowAboutUs = findViewById(R.id.rowAboutUs);
        rowLogout = findViewById(R.id.rowLogout);
        
        imgAvatar = findViewById(R.id.imgAvatar);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtDisplayNameValue = findViewById(R.id.txtDisplayNameValue);
        txtEmailValue = findViewById(R.id.txtEmailValue);
    }

    private void setupListeners() {
        // Xử lý nút Back trên Toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Trở về màn hình trước
            }
        });

        // Click đổi Avatar
        btnEditAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Chọn ảnh từ thư viện...", Toast.LENGTH_SHORT).show();
            }
        });

        // Click sửa Tên
        rowDisplayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });

        // Click sửa Email
        rowEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });

        // Click Về chúng tôi
        if(rowAboutUs != null) {
            rowAboutUs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProfileActivity.this, AboutActivity.class));
                }
            });
        }

        if (rowLogout != null) {
            rowLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.logout_button, (dialog, which) -> performLogout())
                .show();
    }

    private void performLogout() {
        SessionManager.logout(this);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
        Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateData() {
        // Giả sử XP hiện tại là 2450, Max là 3200
        int currentXP = 2450;
        int maxXP = 3200;
        
        // Tính phần trăm cho ProgressBar hiện đại
        int progress = (int) (((float) currentXP / maxXP) * 100);
        progressBarLevel.setProgressCompat(progress, true); // true = có animation mượt
        
        // Lấy dữ liệu Avatar, Tên, Email thực tế
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String displayName = preferences.getString(KEY_DISPLAY_NAME, "Người đọc BookSpace");
        String email = preferences.getString(KEY_EMAIL, "Chưa cập nhật email");
        String avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP";

        if (TextUtils.isEmpty(displayName)) displayName = "Người đọc BookSpace";
        if (TextUtils.isEmpty(email)) email = "Chưa cập nhật email";

        txtProfileName.setText(displayName);
        txtDisplayNameValue.setText(displayName);
        txtEmailValue.setText(email);

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .into(imgAvatar);
    }
}
