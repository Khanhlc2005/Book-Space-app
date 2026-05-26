package com.example.bookspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.core.content.ContextCompat;

import com.example.bookspace.databinding.ActivityFavouriteBinding;
import com.example.bookspace.repository.FavouriteRepository;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity implements FavouriteBookAdapter.OnFavouriteActionListener {
    private ActivityFavouriteBinding binding;
    private FavouriteRepository favouriteRepository;
    private FavouriteBookAdapter adapter;
    private FavouriteManager favouriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavouriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        favouriteRepository = new FavouriteRepository(this);

        setupNavigation();
        setupEmptyState();

        favouriteManager = new FavouriteManager(this);

        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavouriteBookAdapter(new ArrayList<>(), this);
        binding.rvFavorites.setAdapter(adapter);
    }

    private void setupEmptyState() {
        binding.emptyStateFavorite.imgEmptyIcon.setImageResource(R.drawable.ic_favorite_border);
        binding.emptyStateFavorite.txtEmptyMessage.setText("Bạn chưa yêu thích cuốn sách nào. Hãy khám phá thư viện!");
    }

    private void setupNavigation() {
        int activeColor = ContextCompat.getColor(this, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnReminder = findViewById(R.id.btnReminder);
        if (btnReminder != null) {
            btnReminder.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReminderActivity.class);
                startActivity(intent);
            });
        }

        android.widget.TextView tabReading = findViewById(R.id.tabReading);
        if (tabReading != null) {
            tabReading.setOnClickListener(v -> {
                MainActivity.updateBottomNavIcon(this, R.id.nav_library);
                Intent intent = new Intent(this, CurrentlyReadingListActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }

        binding.bottomNav.navHome.setOnClickListener(v -> {
            MainActivity.updateBottomNavIcon(this, R.id.nav_home);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.bottomNav.navReader.setOnClickListener(v -> {
            MainActivity.updateBottomNavIcon(this, R.id.nav_reader);
            Intent intent = new Intent(this, ReadingActivity.class);
            intent.putExtra("SOURCE_PAGE", R.id.nav_library);
            startActivity(intent);
        });

        binding.bottomNav.navLibrary.setOnClickListener(v -> {
            MainActivity.updateBottomNavIcon(this, R.id.nav_library);
        });

        updateBottomNavUi(R.id.nav_library, activeColor, inactiveColor);
    }

    private void updateBottomNavUi(int activeId, int activeColor, int inactiveColor) {
        binding.bottomNav.iconHome.setColorFilter(activeId == R.id.nav_home ? activeColor : inactiveColor);
        binding.bottomNav.textHome.setTextColor(activeId == R.id.nav_home ? activeColor : inactiveColor);

        binding.bottomNav.iconReader.setColorFilter(activeId == R.id.nav_reader ? activeColor : inactiveColor);
        binding.bottomNav.textReader.setTextColor(activeId == R.id.nav_reader ? activeColor : inactiveColor);

        binding.bottomNav.iconLibrary.setColorFilter(activeId == R.id.nav_library ? activeColor : inactiveColor);
        binding.bottomNav.textLibrary.setTextColor(activeId == R.id.nav_library ? activeColor : inactiveColor);
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
        MainActivity.updateBottomNavIcon(this, R.id.nav_library);
    }

    private void loadFavouriteBooks() {
        List<Book> books = favouriteManager.getFauvourites();
        adapter.updateData(books);

        if (binding.emptyStateFavorite.getRoot() != null) {
            if (books == null || books.isEmpty()) {
                binding.rvFavorites.setVisibility(View.GONE);
                binding.emptyStateFavorite.getRoot().setVisibility(View.VISIBLE);
            } else {
                binding.rvFavorites.setVisibility(View.VISIBLE);
                binding.emptyStateFavorite.getRoot().setVisibility(View.GONE);
            }
        }
    }
}
