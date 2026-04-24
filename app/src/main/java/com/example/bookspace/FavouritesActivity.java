package com.example.bookspace;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FavouritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure this matches the name of your Favorites XML file
        setContentView(R.layout.activity_favourite);

        // 1. Find the Đang Đọc tab
        TextView tabReading = findViewById(R.id.tabReading);

        // 2. Set a click listener on the Đang Đọc tab
        tabReading.setOnClickListener(v -> {
            // Create an Intent to go back to the Reading Activity
            Intent intent = new Intent(FavouritesActivity.this, ReadingActivity.class);
            startActivity(intent);

            // Remove the sliding animation
            overridePendingTransition(0, 0);

            // Close this activity
            finish();
        });

        // (You will also set up your RecyclerView for the Favorites list here later)
    }
}