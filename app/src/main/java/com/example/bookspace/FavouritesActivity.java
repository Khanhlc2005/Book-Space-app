package com.example.bookspace;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.repository.FavouriteRepository;

public class FavouritesActivity extends AppCompatActivity {
    private FavouriteRepository favouriteRepository;
    private FavouriteBookAdapter favouriteBookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure this matches the name of your Favorites XML file
        setContentView(R.layout.activity_favourite);
        favouriteRepository = new FavouriteRepository(this);

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

        favouriteBookAdapter = new FavouriteBookAdapter(new FavouriteBookAdapter.Listener() {
            @Override
            public void onBookClick(BookEntity book) {
                BookDetailBottomSheet.show(FavouritesActivity.this, Book.fromEntity(book));
            }

            @Override
            public void onRemoveFavourite(BookEntity book) {
                favouriteRepository.setFavourite(book.id, false);
                loadFavouriteBooks();
            }
        });

        androidx.recyclerview.widget.RecyclerView rvFavorites = findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(favouriteBookAdapter);
        loadFavouriteBooks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (favouriteBookAdapter != null) {
            loadFavouriteBooks();
        }
    }

    private void loadFavouriteBooks() {
        favouriteBookAdapter.submitList(favouriteRepository.getFavouriteBooks());
    }
}
