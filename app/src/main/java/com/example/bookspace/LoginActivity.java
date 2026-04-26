package com.example.bookspace;

import android.content.Context;
import android.content.Intent;
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

import com.example.bookspace.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.isLoggedIn(this)) {
            openMainActivity();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginRoot, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.loginContent.setPadding(
                    binding.loginContent.getPaddingLeft(),
                    insets.top + 12,
                    binding.loginContent.getPaddingRight(),
                    insets.bottom + 16
            );
            return WindowInsetsCompat.CONSUMED;
        });

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = getInputText(binding.loginEmailInput.getText());
        String password = getInputText(binding.loginPasswordInput.getText());

        binding.emailInputLayout.setError(null);
        binding.passwordInputLayout.setError(null);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(email)) {
                binding.emailInputLayout.setError(getString(R.string.login_required_error));
                binding.loginEmailInput.requestFocus();
            } else {
                binding.passwordInputLayout.setError(getString(R.string.login_required_error));
                binding.loginPasswordInput.requestFocus();
            }
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.setError(getString(R.string.login_invalid_email_error));
            binding.loginEmailInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.passwordInputLayout.setError(getString(R.string.login_password_short_error));
            binding.loginPasswordInput.requestFocus();
            return;
        }

        SessionManager.login(this, email);
        syncProfileFromLogin(email);
        hideKeyboard();
        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
        openMainActivity();
    }

    private void syncProfileFromLogin(String email) {
        SharedPreferences preferences = getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE);
        String displayName = preferences.getString(ProfileActivity.KEY_DISPLAY_NAME, "");
        SharedPreferences.Editor editor = preferences.edit()
                .putString(ProfileActivity.KEY_EMAIL, email);

        if (TextUtils.isEmpty(displayName)) {
            editor.putString(ProfileActivity.KEY_DISPLAY_NAME, createDisplayName(email));
        }

        editor.apply();
    }

    private String createDisplayName(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }
        return getString(R.string.profile_default_name);
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
