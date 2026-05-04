package com.example.bookspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FavouritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        setupNavigation();
    }

    private void setupNavigation() {
        // 1. Find the Đang Đọc tab
        TextView tabReading = findViewById(R.id.tabReading);
        if (tabReading != null) {
            tabReading.setOnClickListener(v -> {
                Intent intent = new Intent(FavouritesActivity.this, CurrentlyReadingListActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }

        // Home Navigation in Bottom Nav
        View navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
        
        // Library Navigation in Bottom Nav (Current screen is already part of Library)
        View navLibrary = findViewById(R.id.nav_library);
        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                // Already in library/favorites, maybe just refresh or do nothing
            });
        }
    }
}
