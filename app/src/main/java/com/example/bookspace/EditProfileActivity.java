package com.example.bookspace;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookspace.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.editProfileRoot, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.editProfileTopBar.setPadding(
                    binding.editProfileTopBar.getPaddingLeft(),
                    insets.top + 12,
                    binding.editProfileTopBar.getPaddingRight(),
                    binding.editProfileTopBar.getPaddingBottom()
            );
            binding.editProfileContent.setPadding(
                    binding.editProfileContent.getPaddingLeft(),
                    binding.editProfileContent.getPaddingTop(),
                    binding.editProfileContent.getPaddingRight(),
                    insets.bottom + 24
            );
            return WindowInsetsCompat.CONSUMED;
        });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
        bindCurrentProfile();
    }

    private void bindCurrentProfile() {
        SharedPreferences preferences = getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE);
        String displayName = preferences.getString(ProfileActivity.KEY_DISPLAY_NAME, getString(R.string.profile_default_name));
        String email = preferences.getString(ProfileActivity.KEY_EMAIL, "");

        binding.editNameInput.setText(displayName);
        binding.editEmailInput.setText(email);
        binding.editNameInput.requestFocus();
        binding.editNameInput.setSelection(binding.editNameInput.length());
    }

    private void saveProfile() {
        String displayName = getInputText(binding.editNameInput.getText());
        String email = getInputText(binding.editEmailInput.getText());

        binding.nameInputLayout.setError(null);
        binding.emailInputLayout.setError(null);

        if (TextUtils.isEmpty(displayName)) {
            binding.nameInputLayout.setError(getString(R.string.edit_profile_name_required));
            binding.editNameInput.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.setError(getString(R.string.edit_profile_email_invalid));
            binding.editEmailInput.requestFocus();
            return;
        }

        getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(ProfileActivity.KEY_DISPLAY_NAME, displayName)
                .putString(ProfileActivity.KEY_EMAIL, email)
                .apply();

        hideKeyboard();
        Toast.makeText(this, R.string.edit_profile_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getInputText(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null && getCurrentFocus() != null) {
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
