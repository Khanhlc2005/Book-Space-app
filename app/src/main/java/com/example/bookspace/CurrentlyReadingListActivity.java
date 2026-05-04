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
        setContentView(R.layout.activity_reading_booklist);

        setupNavigation();

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

    private void openReminderActivity() {
        Intent intent = new Intent(this, ReminderActivity.class);
        startActivity(intent);
    }

    private void setupNavigation() {
        // Nút Quay lại
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        TextView tabFavorite = findViewById(R.id.tabFavorite);
        if (tabFavorite != null) {
            tabFavorite.setOnClickListener(v -> {
                Intent intent = new Intent(this, FavouritesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }

        View navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View navReader = findViewById(R.id.imgCurrentReading);
        if (navReader != null) {
            navReader.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReadingActivity.class);
                startActivity(intent);
            });
        }
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
