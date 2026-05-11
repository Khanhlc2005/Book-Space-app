package com.example.bookspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        setContentView(R.layout.activity_library_favourite);
        favouriteRepository = new FavouriteRepository(this);

        setupNavigation();
        setupEmptyState();
    }

    private void setupEmptyState() {
        View emptyState = findViewById(R.id.emptyStateFavorite);        
        if (emptyState != null) {
            android.widget.ImageView imgEmptyIcon = emptyState.findViewById(R.id.imgEmptyIcon);
            android.widget.TextView txtEmptyMessage = emptyState.findViewById(R.id.txtEmptyMessage);
            
            if (imgEmptyIcon != null) {
                imgEmptyIcon.setImageResource(R.drawable.ic_favorite_border);
            }
            if (txtEmptyMessage != null) {
                txtEmptyMessage.setText("Bạn chưa yêu thích cuốn sách nào. Hãy khám phá thư viện!");
            }
        }
    }

    private void setupNavigation() {
        BottomNavManager.setupBottomNav(this, BottomNavManager.NAV_LIBRARY);

        TextView tabReading = findViewById(R.id.tabReading);
        if (tabReading != null) {
            tabReading.setOnClickListener(v -> {
                Intent intent = new Intent(FavouritesActivity.this, CurrentlyReadingListActivity.class);
                startActivity(intent);
                finish();
            });
        }

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
        BottomNavManager.setupBottomNav(this, BottomNavManager.NAV_LIBRARY);
        if (favouriteBookAdapter != null) {
            loadFavouriteBooks();
        }
    }

    private void loadFavouriteBooks() {
        java.util.List<BookEntity> books = favouriteRepository.getFavouriteBooks();
        favouriteBookAdapter.submitList(books);
        
        View emptyState = findViewById(R.id.emptyStateFavorite);
        View rvFavorites = findViewById(R.id.rvFavorites);
        
        if (emptyState != null && rvFavorites != null) {
            if (books == null || books.isEmpty()) {
                rvFavorites.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                rvFavorites.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        }
    }
}
