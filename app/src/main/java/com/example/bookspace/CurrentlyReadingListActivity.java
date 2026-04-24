package com.example.bookspace;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CurrentlyReadingListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure this matches the name of your Reading XML file
        setContentView(R.layout.activity_reading_booklist);

        // 1. Find the Yêu Thích tab
        TextView tabFavorite = findViewById(R.id.tabFavorite);

        // 2. Set a click listener on the Yêu Thích tab
        tabFavorite.setOnClickListener(v -> {
            // Create an Intent to go to the Favorites Activity
            Intent intent = new Intent(CurrentlyReadingListActivity.this, FavouritesActivity.class);
            startActivity(intent);

            // This removes the sliding animation so it feels like a real tab switch!
            overridePendingTransition(0, 0);

            // Close this activity so the back button doesn't get confusing
            finish();
        });

        // (You will also set up your RecyclerView for the Reading list here later)
    }
}

