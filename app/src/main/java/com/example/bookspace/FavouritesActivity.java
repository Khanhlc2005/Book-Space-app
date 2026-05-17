package com.example.bookspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookspace.database.entity.BookEntity;
import com.example.bookspace.repository.FavouriteRepository;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity implements FavouriteBookAdapter.OnFavouriteActionListener {
    private FavouriteRepository favouriteRepository;
    private FavouriteBookAdapter adapter;
    private FavouriteManager favouriteManager;
    private RecyclerView rvFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        favouriteRepository = new FavouriteRepository(this);

        setupNavigation();
        setupEmptyState();
        
        favouriteManager = new FavouriteManager(this);

        // Thiết lập RecyclerView
        rvFavorites = findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavouriteBookAdapter(new ArrayList<>(), this);
        rvFavorites.setAdapter(adapter);
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
        // Nút Quay lại
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Nút Nhắc nhở
        View btnReminder = findViewById(R.id.btnReminder);
        if (btnReminder != null) {
            btnReminder.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReminderActivity.class);
                startActivity(intent);
            });
        }

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
        
        // Reader Navigation in Bottom Nav
        View navReader = findViewById(R.id.nav_reader);
        if (navReader != null) {
            navReader.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReadingActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onRemoveFavourite(Book book, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xóa sách")
            .setMessage("Bạn có chắc chắn muốn xóa sách '" + book.getTitle() + "' khỏi danh sách yêu thích không?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                // Bỏ yêu thích trong DB
                favouriteManager.toggleFavourite(book.getId());
                // Xóa khỏi danh sách UI
                adapter.removeItem(position);
                Toast.makeText(this, "Đã xóa: " + book.getTitle(), Toast.LENGTH_SHORT).show();
                loadFavouriteBooks();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public void onBookClick(Book book) {
        // Mở ReadingActivity với bookId
        Intent intent = new Intent(this, ReadingActivity.class);
        intent.putExtra("BOOK_ID", book.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadFavouriteBooks();
        }
    }

    private void loadFavouriteBooks() {
        List<Book> books = favouriteManager.getFauvourites();
        adapter.updateData(books);
        
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
