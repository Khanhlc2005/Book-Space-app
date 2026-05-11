package com.example.bookspace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CurrentlyReadingListActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_reading);

        setupNavigation();
        setupEmptyState();

        // Setup Reminder Button - Mở danh sách báo thức kiểu iPhone
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        if (btnReminder != null) {
            btnReminder.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
                    } else {
                        openReminderActivity();
                    }
                } else {
                    openReminderActivity();
                }
            });
        }
    }

    private void setupEmptyState() {
        View emptyState = findViewById(R.id.emptyStateReading);
        View rvReading = findViewById(R.id.rvReading);
        
        if (emptyState != null && rvReading != null) {
            // Mặc định hiện trạng thái rỗng do chưa có dữ liệu thật
            rvReading.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            
            android.widget.ImageView imgEmptyIcon = emptyState.findViewById(R.id.imgEmptyIcon);
            android.widget.TextView txtEmptyMessage = emptyState.findViewById(R.id.txtEmptyMessage);
            
            if (imgEmptyIcon != null) {
                imgEmptyIcon.setImageResource(R.drawable.ic_auto_stories);
            }
            if (txtEmptyMessage != null) {
                txtEmptyMessage.setText("Bạn chưa đọc cuốn sách nào. Bắt đầu đọc ngay!");
            }
        }
    }


    private void openReminderActivity() {
        Intent intent = new Intent(this, ReminderActivity.class);
        startActivity(intent);
    }

    private void setupNavigation() {
        BottomNavManager.setupBottomNav(this, BottomNavManager.NAV_LIBRARY);

        TextView tabFavorite = findViewById(R.id.tabFavorite);
        if (tabFavorite != null) {
            tabFavorite.setOnClickListener(v -> {
                Intent intent = new Intent(this, FavouritesActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavManager.setupBottomNav(this, BottomNavManager.NAV_LIBRARY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openReminderActivity();
            } else {
                Toast.makeText(this, "Cần quyền thông báo để quản lý nhắc nhở", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
