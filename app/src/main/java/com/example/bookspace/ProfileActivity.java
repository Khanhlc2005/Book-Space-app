package com.example.bookspace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {
    
    public static final String PREFS_NAME = "bookspace_profile";
    public static final String KEY_DISPLAY_NAME = "display_name";
    public static final String KEY_EMAIL = "email";

    private AppBarLayout appBarLayout;
    private MaterialToolbar toolbar;
    private FloatingActionButton btnEditAvatar;
    private ConstraintLayout rowDisplayName, rowEmail;
    private LinearProgressIndicator progressBarLevel;
    private MaterialCardView rowAboutUs, rowLogout;
    
    private ImageView imgAvatar;
    private TextView txtProfileName, txtDisplayNameValue, txtEmailValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Ánh xạ View
        initViews();
        
        // Thiết lập sự kiện
        setupListeners();
        
        // Cập nhật dữ liệu động (Ví dụ ProgressBar)
        updateData();
    }

    private void initViews() {
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        rowDisplayName = findViewById(R.id.rowDisplayName);
        rowEmail = findViewById(R.id.rowEmail);
        progressBarLevel = findViewById(R.id.progressBarLevel);
        rowAboutUs = findViewById(R.id.rowAboutUs);
        rowLogout = findViewById(R.id.rowLogout);
        
        imgAvatar = findViewById(R.id.imgAvatar);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtDisplayNameValue = findViewById(R.id.txtDisplayNameValue);
        txtEmailValue = findViewById(R.id.txtEmailValue);
    }

    private void setupListeners() {
        // Xử lý nút Back trên Toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Trở về màn hình trước
            }
        });

        // Click đổi Avatar
        btnEditAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Chọn ảnh từ thư viện...", Toast.LENGTH_SHORT).show();
            }
        });

        // Click sửa Tên
        rowDisplayName.setOnClickListener(v -> showEditProfileBottomSheet(true));

        // Click sửa Email
        rowEmail.setOnClickListener(v -> showEditProfileBottomSheet(false));

        // Click Về chúng tôi
        if(rowAboutUs != null) {
            rowAboutUs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProfileActivity.this, AboutActivity.class));
                }
            });
        }

        if (rowLogout != null) {
            rowLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void showEditProfileBottomSheet(boolean focusName) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_profile, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextInputLayout nameInputLayout = bottomSheetView.findViewById(R.id.nameInputLayout);
        TextInputLayout emailInputLayout = bottomSheetView.findViewById(R.id.emailInputLayout);
        TextInputEditText editNameInput = bottomSheetView.findViewById(R.id.editNameInput);
        TextInputEditText editEmailInput = bottomSheetView.findViewById(R.id.editEmailInput);
        MaterialButton btnCancelProfileEdit = bottomSheetView.findViewById(R.id.btnCancelProfileEdit);
        MaterialButton btnSaveProfile = bottomSheetView.findViewById(R.id.btnSaveProfile);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editNameInput.setText(preferences.getString(KEY_DISPLAY_NAME, getString(R.string.profile_default_name)));
        editEmailInput.setText(preferences.getString(KEY_EMAIL, ""));

        btnCancelProfileEdit.setOnClickListener(v -> {
            hideKeyboard(bottomSheetView);
            bottomSheetDialog.dismiss();
        });

        btnSaveProfile.setOnClickListener(v -> {
            if (saveProfileFromBottomSheet(nameInputLayout, emailInputLayout, editNameInput, editEmailInput)) {
                hideKeyboard(bottomSheetView);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }

            TextInputEditText targetInput = focusName ? editNameInput : editEmailInput;
            targetInput.requestFocus();
            targetInput.setSelection(targetInput.length());
            targetInput.postDelayed(() -> {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null) {
                    manager.showSoftInput(targetInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 150);
        });

        bottomSheetDialog.show();
    }

    private boolean saveProfileFromBottomSheet(TextInputLayout nameInputLayout,
                                               TextInputLayout emailInputLayout,
                                               TextInputEditText editNameInput,
                                               TextInputEditText editEmailInput) {
        String displayName = getInputText(editNameInput.getText());
        String email = getInputText(editEmailInput.getText());

        nameInputLayout.setError(null);
        emailInputLayout.setError(null);

        if (TextUtils.isEmpty(displayName)) {
            nameInputLayout.setError(getString(R.string.edit_profile_name_required));
            editNameInput.requestFocus();
            return false;
        }

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.edit_profile_email_invalid));
            editEmailInput.requestFocus();
            return false;
        }

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_DISPLAY_NAME, displayName)
                .putString(KEY_EMAIL, email)
                .apply();

        updateData();
        Toast.makeText(this, R.string.edit_profile_saved, Toast.LENGTH_SHORT).show();
        return true;
    }

    private String getInputText(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null && view != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.logout_button, (dialog, which) -> performLogout())
                .show();
    }

    private void performLogout() {
        SessionManager.logout(this);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
        Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateData() {
        // Giả sử XP hiện tại là 2450, Max là 3200
        int currentXP = 2450;
        int maxXP = 3200;
        
        // Tính phần trăm cho ProgressBar hiện đại
        int progress = (int) (((float) currentXP / maxXP) * 100);
        progressBarLevel.setProgressCompat(progress, true); // true = có animation mượt
        
        // Lấy dữ liệu Avatar, Tên, Email thực tế
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String displayName = preferences.getString(KEY_DISPLAY_NAME, "Người đọc BookSpace");
        String email = preferences.getString(KEY_EMAIL, "Chưa cập nhật email");
        String avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuChsxoWmzwCRstgLqcTDca1SbPewXFrd0uJ5OY1FXuxAbdAscBM9j6kIhXhpstpImEZ9gAb_dxSYbqQ89m8NaPr6el5OQ5Z2YUeNfDh0DY4W0jb1KgYJVGAhvrANoMbLUrLg6s2DwyywmvegE394jntrgSqpxeej_IVKMPbHm8FqQoKbRYehHyNI1CF5738hoct6Bq7hD7ropM4BGBt9-geFXn1Cn9dj1fImBsanHfifcxjGf18spz-dcrPi17FerhLiXzmbr4o2FiP";

        if (TextUtils.isEmpty(displayName)) displayName = "Người đọc BookSpace";
        if (TextUtils.isEmpty(email)) email = "Chưa cập nhật email";

        txtProfileName.setText(displayName);
        txtDisplayNameValue.setText(displayName);
        txtEmailValue.setText(email);

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .into(imgAvatar);
    }
}
