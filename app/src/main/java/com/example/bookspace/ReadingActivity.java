package com.example.bookspace;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import com.example.bookspace.databinding.ActivityMainBinding;

import com.example.bookspace.databinding.ActivityReadingBinding;

public class ReadingActivity extends AppCompatActivity {

    private ActivityReadingBinding binding;

    // Reading font sizes (sp values scaled to px at runtime)
    private final int[] fontSizes = {16, 17, 18, 19, 20, 21, 22};
    private int fontSizeIndex = 3; // default 19sp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupTopBar();
        setupSettingsPanel();
        setupTextSizeButtons();
        setupFontButtons();
        setupThemeButtons();
        setupScrollThumb();
    }

    // ── Top Bar ──
    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSettings.setOnClickListener(v -> {
            boolean isVisible = binding.settingsPanel.getVisibility() == View.VISIBLE;
            binding.settingsPanel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        // Clicking outside settings panel closes it
        binding.readingScrollView.setOnClickListener(v ->
                binding.settingsPanel.setVisibility(View.GONE));
    }

    // ── Settings Panel ──
    private void setupSettingsPanel() {
        binding.sliderBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                // Brightness control via WindowManager (optional, requires WRITE_SETTINGS permission)
                // android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
                // lp.screenBrightness = p / 100f;
                // getWindow().setAttributes(lp);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    // ── Text Size ──
    private void setupTextSizeButtons() {
        binding.btnTextMinus.setOnClickListener(v -> changeFontSize(-1));
        binding.btnTextPlus.setOnClickListener(v -> changeFontSize(1));
    }

    private void changeFontSize(int direction) {
        fontSizeIndex = Math.max(0, Math.min(fontSizes.length - 1, fontSizeIndex + direction));
        float size = fontSizes[fontSizeIndex];
        int pct = Math.round((size / fontSizes[3]) * 100);
        binding.btnText100.setText(pct + "%");

        // Apply to all reading paragraphs
        binding.tvParagraph1.setTextSize(size);
        binding.tvParagraph2.setTextSize(size);
        binding.tvParagraph3.setTextSize(size);
        binding.tvParagraph4.setTextSize(size);
        binding.tvParagraph5.setTextSize(size);
        binding.tvPullQuote.setTextSize(size);
    }

    // ── Font Toggle ──
    private void setupFontButtons() {
        binding.btnFontLiterata.setOnClickListener(v -> {
            // Literata is a serif (fallback to default serif if not bundled)
            binding.btnFontLiterata.setBackgroundResource(R.drawable.reading_btn_bg);
            binding.btnFontInter.setBackground(null);
        });

        binding.btnFontInter.setOnClickListener(v -> {
            binding.btnFontInter.setBackgroundResource(R.drawable.reading_btn_bg);
            binding.btnFontLiterata.setBackground(null);
        });
    }

    // ── Theme ──
    private void setupThemeButtons() {
        binding.btnThemeLight.setOnClickListener(v -> applyTheme(
                R.color.reading_bg, R.color.reading_text,
                R.color.reading_accent, R.color.reading_text_muted));

        binding.btnThemeSepia.setOnClickListener(v -> applyTheme(
                R.color.reading_bg_sepia, R.color.reading_text_sepia,
                R.color.reading_accent_sepia, R.color.reading_text_muted_sepia));

        binding.btnThemeDark.setOnClickListener(v -> applyTheme(
                R.color.reading_bg_dark, R.color.reading_text_dark,
                R.color.reading_accent_dark, R.color.reading_text_muted_dark));
    }

    private void applyTheme(int bgColor, int textColor, int accentColor, int mutedColor) {
        int bg     = getColor(bgColor);
        int text   = getColor(textColor);
        int accent = getColor(accentColor);
        int muted  = getColor(mutedColor);

        binding.readingRoot.setBackgroundColor(bg);
        binding.topBar.setBackgroundColor(bg);
        binding.bottomStatusBar.setBackgroundColor(bg);

        binding.tvParagraph1.setTextColor(text);
        binding.tvParagraph2.setTextColor(text);
        binding.tvParagraph3.setTextColor(text);
        binding.tvParagraph4.setTextColor(text);
        binding.tvParagraph5.setTextColor(text);
        binding.tvPullQuote.setTextColor(muted);

        binding.tvBookTitle.setTextColor(accent);
        binding.tvChapterLabel.setTextColor(muted);
        binding.tvProgressPercent.setTextColor(accent);
        binding.tvPageInfo.setTextColor(muted);
    }


    private void setupScrollThumb() {
        View thumb = binding.scrollThumb;
        View track = binding.scrollTrackLine;

        binding.readingScrollView.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldX, oldY) -> {
                    int totalScroll = v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight();
                    if (totalScroll <= 0) return;

                    float pct = (float) scrollY / totalScroll;
                    int trackH = track.getHeight();
                    int thumbH = thumb.getHeight();
                    int maxTop = trackH - thumbH;

                    int topOffset = binding.topBar.getHeight();
                    float newY = topOffset + (pct * maxTop);
                    thumb.setY(newY);
                    thumb.setAlpha(0.85f);

                    int displayPct = Math.round(pct * 100);
                    binding.tvProgressPercent.setText(displayPct + "% COMPLETED");
                });

        thumb.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    thumb.setAlpha(1f);
                    break;

                case android.view.MotionEvent.ACTION_MOVE: {
                    int topOffset = binding.topBar.getHeight();
                    int trackH    = track.getHeight();
                    int thumbH    = thumb.getHeight();
                    int maxTop    = trackH - thumbH;

                    float newY = event.getRawY() - thumbH / 2f;
                    newY = Math.max(topOffset, Math.min(topOffset + maxTop, newY));
                    thumb.setY(newY);

                    float pct = maxTop > 0 ? (newY - topOffset) / maxTop : 0;
                    int totalScroll =
                            binding.readingScrollView.getChildAt(0).getMeasuredHeight()
                                    - binding.readingScrollView.getMeasuredHeight();
                    binding.readingScrollView.scrollTo(0, (int)(pct * totalScroll));
                    break;
                }

                case android.view.MotionEvent.ACTION_UP:
                    thumb.setAlpha(0.85f);
                    break;
            }
            return true;
        });
    }
}