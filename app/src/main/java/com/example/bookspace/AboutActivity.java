package com.example.bookspace;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookspace.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {
    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.aboutRoot, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.aboutTopBar.setPadding(
                    binding.aboutTopBar.getPaddingLeft(),
                    insets.top + 12,
                    binding.aboutTopBar.getPaddingRight(),
                    binding.aboutTopBar.getPaddingBottom()
            );
            binding.aboutContent.setPadding(
                    binding.aboutContent.getPaddingLeft(),
                    binding.aboutContent.getPaddingTop(),
                    binding.aboutContent.getPaddingRight(),
                    insets.bottom + 24
            );
            return WindowInsetsCompat.CONSUMED;
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
