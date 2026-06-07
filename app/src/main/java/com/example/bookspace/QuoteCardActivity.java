package com.example.bookspace;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookspace.databinding.ActivityQuoteCardBinding;
import com.example.bookspace.quote.QuoteCardExporter;
import com.example.bookspace.quote.QuoteCardPreset;
import com.example.bookspace.quote.QuoteCardStyle;
import com.example.bookspace.quote.QuotePatternView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class QuoteCardActivity extends AppCompatActivity {
    public static final String EXTRA_QUOTE_TEXT = "extra_quote_text";
    public static final String EXTRA_BOOK_TITLE = "extra_book_title";
    public static final String EXTRA_AUTHOR_NAME = "extra_author_name";

    private static final String TAG = "QuoteCardActivity";
    private static final int REQUEST_WRITE_STORAGE = 3201;
    private static final int TEXT_SIZE_MIN = 14;
    private static final int TEXT_SIZE_MAX = 52;

    private static final String STATE_BACKGROUND = "state_background";
    private static final String STATE_PATTERN = "state_pattern";
    private static final String STATE_FONT = "state_font";
    private static final String STATE_TEXT_SIZE = "state_text_size";
    private static final String STATE_TEXT_COLOR = "state_text_color";
    private static final String STATE_GRAVITY = "state_gravity";
    private static final String STATE_MANUAL_TEXT_COLOR = "state_manual_text_color";

    private ActivityQuoteCardBinding binding;
    private QuoteCardStyle selectedStyle;

    private final List<QuoteCardPreset> presets = new ArrayList<>();
    private final List<BackgroundOption> backgroundOptions = new ArrayList<>();
    private final List<PatternOption> patternOptions = new ArrayList<>();
    private final List<FontOption> fontOptions = new ArrayList<>();
    private final List<TextColorOption> textColorOptions = new ArrayList<>();

    private final List<View> presetViews = new ArrayList<>();
    private final List<View> backgroundViews = new ArrayList<>();
    private final List<View> patternViews = new ArrayList<>();
    private final List<MaterialButton> fontButtons = new ArrayList<>();
    private final List<View> textColorViews = new ArrayList<>();
    private final List<ImageButton> alignButtons = new ArrayList<>();

    private String quoteText;
    private String bookTitle;
    private String authorName;
    private boolean updatingTextSizeInput = false;

    public static Intent createIntent(Context context, String quoteText, String bookTitle, String authorName) {
        Intent intent = new Intent(context, QuoteCardActivity.class);
        intent.putExtra(EXTRA_QUOTE_TEXT, quoteText);
        intent.putExtra(EXTRA_BOOK_TITLE, bookTitle);
        intent.putExtra(EXTRA_AUTHOR_NAME, authorName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityQuoteCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedStyle = new QuoteCardStyle(ContextCompat.getColor(this, R.color.quote_text_dark_brown));
        readIntentData();
        setupOptionData();
        restoreStyle(savedInstanceState);
        initViews();
        setupToolbar();
        setupPreview();
        setupPresetOptions();
        setupBackgroundOptions();
        setupPatternOptions();
        setupFontOptions();
        setupTextSizeControls();
        setupTextColorOptions();
        setupAlignmentControls();
        applyStyle();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_BACKGROUND, selectedStyle.backgroundPreset);
        outState.putInt(STATE_PATTERN, selectedStyle.patternPreset);
        outState.putInt(STATE_FONT, selectedStyle.fontPreset);
        outState.putInt(STATE_TEXT_SIZE, selectedStyle.textSizeSp);
        outState.putInt(STATE_TEXT_COLOR, selectedStyle.textColor);
        outState.putInt(STATE_GRAVITY, selectedStyle.textGravity);
        outState.putBoolean(STATE_MANUAL_TEXT_COLOR, selectedStyle.textColorManuallyChanged);
    }

    private void initViews() {
        binding.txtQuote.setText(quoteText);
        String source = buildQuoteSource();
        binding.txtQuoteSource.setText(source);
        binding.txtQuoteSource.setVisibility(TextUtils.isEmpty(source) ? View.GONE : View.VISIBLE);
        binding.btnSaveImage.setOnClickListener(v -> saveQuoteCard());
        binding.btnShareImage.setOnClickListener(v -> shareQuoteCard());
    }

    private void readIntentData() {
        Intent intent = getIntent();
        quoteText = trimToEmpty(intent.getStringExtra(EXTRA_QUOTE_TEXT));
        bookTitle = trimToEmpty(intent.getStringExtra(EXTRA_BOOK_TITLE));
        authorName = trimToEmpty(intent.getStringExtra(EXTRA_AUTHOR_NAME));

        if (TextUtils.isEmpty(quoteText)) {
            // Fallback chi dung de kiem thu man hinh khi team Quote/Bookmark chua truyen du lieu.
            quoteText = getString(R.string.quote_card_sample_quote);
            if (TextUtils.isEmpty(bookTitle)) {
                bookTitle = getString(R.string.quote_card_sample_book);
            }
            Log.d(TAG, "Quote text is empty. Showing fallback quote for testing.");
            Toast.makeText(this, R.string.quote_card_fallback_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupOptionData() {
        setupBackgroundData();
        setupPresetData();
        setupPatternData();
        setupFontData();
        setupTextColorData();
    }

    private void setupBackgroundData() {
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_IVORY, R.string.quote_card_bg_cream, R.color.quote_bg_ivory_start, R.color.quote_bg_ivory_end, false));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_PAPER, R.string.quote_card_bg_paper, R.color.quote_bg_paper_start, R.color.quote_bg_paper_end, false));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_BEIGE, R.string.quote_card_bg_cream, R.color.quote_bg_beige_start, R.color.quote_bg_beige_end, false));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_LIGHT_BROWN, R.string.quote_card_color_brown, R.color.quote_bg_light_brown_start, R.color.quote_bg_light_brown_end, false));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_LIGHT_GRAY, R.string.quote_card_color_dark, R.color.quote_bg_light_gray_start, R.color.quote_bg_light_gray_end, false));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_DARK_GRAY, R.string.quote_card_color_dark, R.color.quote_bg_dark_gray_start, R.color.quote_bg_dark_gray_end, true));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_BLACK, R.string.quote_card_color_dark, R.color.quote_bg_black_start, R.color.quote_bg_black_end, true));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_NAVY, R.string.quote_card_color_dark, R.color.quote_bg_navy_start, R.color.quote_bg_navy_end, true));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_DEEP_GREEN, R.string.quote_card_bg_sage, R.color.quote_bg_deep_green_start, R.color.quote_bg_deep_green_end, true));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_DEEP_PURPLE, R.string.quote_card_color_brown, R.color.quote_bg_deep_purple_start, R.color.quote_bg_deep_purple_end, true));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_SOFT_PINK, R.string.quote_card_bg_peach, R.color.quote_bg_soft_pink_start, R.color.quote_bg_soft_pink_end, false));
        backgroundOptions.add(new BackgroundOption(QuoteCardStyle.BACKGROUND_WARM_GRADIENT, R.string.quote_card_bg_peach, R.color.quote_bg_warm_gradient_start, R.color.quote_bg_warm_gradient_end, true));
    }

    private void setupPresetData() {
        presets.add(new QuoteCardPreset(R.string.quote_card_preset_classic_paper, QuoteCardStyle.BACKGROUND_PAPER, QuoteCardStyle.PATTERN_PAPER, QuoteCardStyle.FONT_SERIF, R.color.quote_text_dark_brown, 24, Gravity.CENTER));
        presets.add(new QuoteCardPreset(R.string.quote_card_preset_minimal_white, QuoteCardStyle.BACKGROUND_IVORY, QuoteCardStyle.PATTERN_NONE, QuoteCardStyle.FONT_SANS, R.color.quote_text_black, 23, Gravity.CENTER));
        presets.add(new QuoteCardPreset(R.string.quote_card_preset_dark_academia, QuoteCardStyle.BACKGROUND_BLACK, QuoteCardStyle.PATTERN_VIGNETTE, QuoteCardStyle.FONT_CLASSIC, R.color.quote_text_cream, 25, Gravity.CENTER));
        presets.add(new QuoteCardPreset(R.string.quote_card_preset_warm_sunset, QuoteCardStyle.BACKGROUND_WARM_GRADIENT, QuoteCardStyle.PATTERN_LIGHT, QuoteCardStyle.FONT_BOLD, R.color.quote_card_text_light, 24, Gravity.CENTER));
        presets.add(new QuoteCardPreset(R.string.quote_card_preset_calm_green, QuoteCardStyle.BACKGROUND_DEEP_GREEN, QuoteCardStyle.PATTERN_DOTS, QuoteCardStyle.FONT_SERIF, R.color.quote_text_cream, 24, Gravity.CENTER));
        presets.add(new QuoteCardPreset(R.string.quote_card_preset_night_reading, QuoteCardStyle.BACKGROUND_NAVY, QuoteCardStyle.PATTERN_LINES, QuoteCardStyle.FONT_SANS, R.color.quote_card_text_light, 23, Gravity.CENTER));
    }

    private void setupPatternData() {
        patternOptions.add(new PatternOption(QuoteCardStyle.PATTERN_NONE, R.string.quote_card_pattern_none));
        patternOptions.add(new PatternOption(QuoteCardStyle.PATTERN_PAPER, R.string.quote_card_pattern_paper));
        patternOptions.add(new PatternOption(QuoteCardStyle.PATTERN_DOTS, R.string.quote_card_pattern_dots));
        patternOptions.add(new PatternOption(QuoteCardStyle.PATTERN_LINES, R.string.quote_card_pattern_lines));
        patternOptions.add(new PatternOption(QuoteCardStyle.PATTERN_VIGNETTE, R.string.quote_card_pattern_vignette));
        patternOptions.add(new PatternOption(QuoteCardStyle.PATTERN_LIGHT, R.string.quote_card_pattern_light));
    }

    private void setupFontData() {
        fontOptions.add(new FontOption(QuoteCardStyle.FONT_SANS, R.string.quote_card_font_sans, Typeface.SANS_SERIF));
        fontOptions.add(new FontOption(QuoteCardStyle.FONT_SERIF, R.string.quote_card_font_serif, Typeface.SERIF));
        fontOptions.add(new FontOption(QuoteCardStyle.FONT_MONO, R.string.quote_card_font_mono, Typeface.MONOSPACE));
        fontOptions.add(new FontOption(QuoteCardStyle.FONT_BOLD, R.string.quote_card_font_bold, Typeface.DEFAULT_BOLD));
        fontOptions.add(new FontOption(QuoteCardStyle.FONT_CLASSIC, R.string.quote_card_font_classic, Typeface.create(Typeface.SERIF, Typeface.ITALIC)));
    }

    private void setupTextColorData() {
        textColorOptions.add(new TextColorOption(R.color.quote_text_black, R.string.quote_card_color_dark));
        textColorOptions.add(new TextColorOption(R.color.quote_card_text_light, R.string.quote_card_color_light));
        textColorOptions.add(new TextColorOption(R.color.quote_text_cream, R.string.quote_card_bg_cream));
        textColorOptions.add(new TextColorOption(R.color.quote_text_dark_brown, R.string.quote_card_color_brown));
        textColorOptions.add(new TextColorOption(R.color.quote_text_dark_gray, R.string.quote_card_color_dark));
        textColorOptions.add(new TextColorOption(R.color.quote_text_navy, R.string.quote_card_color_dark));
        textColorOptions.add(new TextColorOption(R.color.quote_text_wine, R.string.quote_card_color_brown));
        textColorOptions.add(new TextColorOption(R.color.quote_text_deep_green, R.string.quote_card_bg_sage));
        textColorOptions.add(new TextColorOption(R.color.quote_text_pale_yellow, R.string.quote_card_color_light));
        textColorOptions.add(new TextColorOption(R.color.quote_text_deep_purple, R.string.quote_card_color_dark));
    }

    private void restoreStyle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        selectedStyle.backgroundPreset = savedInstanceState.getInt(STATE_BACKGROUND, selectedStyle.backgroundPreset);
        selectedStyle.patternPreset = savedInstanceState.getInt(STATE_PATTERN, selectedStyle.patternPreset);
        selectedStyle.fontPreset = savedInstanceState.getInt(STATE_FONT, selectedStyle.fontPreset);
        selectedStyle.textSizeSp = savedInstanceState.getInt(STATE_TEXT_SIZE, selectedStyle.textSizeSp);
        selectedStyle.textColor = savedInstanceState.getInt(STATE_TEXT_COLOR, selectedStyle.textColor);
        selectedStyle.textGravity = savedInstanceState.getInt(STATE_GRAVITY, selectedStyle.textGravity);
        selectedStyle.textColorManuallyChanged = savedInstanceState.getBoolean(STATE_MANUAL_TEXT_COLOR, false);
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(binding.quoteCardRoot, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.quoteTopBar.setPadding(
                    binding.quoteTopBar.getPaddingLeft(),
                    insets.top + 12,
                    binding.quoteTopBar.getPaddingRight(),
                    binding.quoteTopBar.getPaddingBottom()
            );
            binding.quoteActionBar.setPadding(
                    binding.quoteActionBar.getPaddingLeft(),
                    binding.quoteActionBar.getPaddingTop(),
                    binding.quoteActionBar.getPaddingRight(),
                    insets.bottom + 16
            );
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupPreview() {
        binding.txtQuote.setMaxLines(quoteText.length() > 420 ? 14 : 12);
    }

    private void setupPresetOptions() {
        binding.presetOptions.removeAllViews();
        presetViews.clear();
        for (QuoteCardPreset preset : presets) {
            View view = createPresetOptionView(preset);
            view.setOnClickListener(v -> {
                selectedStyle.backgroundPreset = preset.backgroundPreset;
                selectedStyle.patternPreset = preset.patternPreset;
                selectedStyle.fontPreset = preset.fontPreset;
                selectedStyle.textColor = ContextCompat.getColor(this, preset.textColorResId);
                selectedStyle.textSizeSp = preset.textSizeSp;
                selectedStyle.textGravity = preset.textGravity;
                selectedStyle.textColorManuallyChanged = false;
                applyStyle();
            });
            presetViews.add(view);
            binding.presetOptions.addView(view);
        }
    }

    private void setupBackgroundOptions() {
        binding.backgroundOptions.removeAllViews();
        backgroundViews.clear();
        for (BackgroundOption option : backgroundOptions) {
            View view = createSwatchView(option.startColorResId, option.endColorResId, dp(46), dp(46));
            view.setContentDescription(getString(R.string.quote_card_cd_background_format, getString(option.nameResId)));
            view.setOnClickListener(v -> {
                selectedStyle.backgroundPreset = option.id;
                if (!selectedStyle.textColorManuallyChanged) {
                    selectedStyle.textColor = getSuggestedTextColor(option);
                }
                applyStyle();
            });
            backgroundViews.add(view);
            binding.backgroundOptions.addView(view);
        }
    }

    private void setupPatternOptions() {
        binding.patternOptions.removeAllViews();
        patternViews.clear();
        for (PatternOption option : patternOptions) {
            View view = createPatternOptionView(option);
            view.setOnClickListener(v -> {
                selectedStyle.patternPreset = option.id;
                applyStyle();
            });
            patternViews.add(view);
            binding.patternOptions.addView(view);
        }
    }

    private void setupFontOptions() {
        binding.fontOptions.removeAllViews();
        fontButtons.clear();
        for (FontOption option : fontOptions) {
            MaterialButton button = new MaterialButton(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
            params.setMarginEnd(dp(8));
            button.setLayoutParams(params);
            button.setMinWidth(dp(74));
            button.setText(option.nameResId);
            button.setTextSize(14);
            button.setAllCaps(false);
            button.setTypeface(option.typeface);
            button.setCornerRadius(dp(21));
            button.setStrokeWidth(dp(1));
            button.setOnClickListener(v -> {
                selectedStyle.fontPreset = option.id;
                applyStyle();
            });
            fontButtons.add(button);
            binding.fontOptions.addView(button);
        }
    }

    private void setupTextSizeControls() {
        binding.sliderTextSize.setMax(TEXT_SIZE_MAX - TEXT_SIZE_MIN);
        binding.sliderTextSize.setProgress(selectedStyle.textSizeSp - TEXT_SIZE_MIN);
        binding.sliderTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                updateTextSize(TEXT_SIZE_MIN + progress, true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                clampTextSizeInput();
            }
        });

        binding.inputTextSize.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.inputTextSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (updatingTextSizeInput) {
                    return;
                }
                handleTextSizeInput(s.toString(), false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.inputTextSize.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                clampTextSizeInput();
            }
        });
        binding.inputTextSize.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clampTextSizeInput();
                binding.inputTextSize.clearFocus();
                return true;
            }
            return false;
        });
    }

    private void setupTextColorOptions() {
        binding.textColorOptions.removeAllViews();
        textColorViews.clear();
        for (TextColorOption option : textColorOptions) {
            View view = createColorOptionView(option);
            view.setOnClickListener(v -> {
                selectedStyle.textColor = ContextCompat.getColor(this, option.colorResId);
                selectedStyle.textColorManuallyChanged = true;
                applyStyle();
            });
            textColorViews.add(view);
            binding.textColorOptions.addView(view);
        }
    }

    private void setupAlignmentControls() {
        binding.alignmentOptions.removeAllViews();
        alignButtons.clear();
        addAlignButton(Gravity.START, R.drawable.ic_align_left, R.string.quote_card_cd_align_left);
        addAlignButton(Gravity.CENTER, R.drawable.ic_align_center, R.string.quote_card_cd_align_center);
        addAlignButton(Gravity.END, R.drawable.ic_align_right, R.string.quote_card_cd_align_right);
    }

    private void addAlignButton(int gravity, int iconResId, int descriptionResId) {
        ImageButton button = new ImageButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(48), dp(42));
        params.setMarginEnd(dp(8));
        button.setLayoutParams(params);
        button.setImageResource(iconResId);
        button.setContentDescription(getString(descriptionResId));
        button.setPadding(dp(11), dp(9), dp(11), dp(9));
        button.setBackground(createSelectorBackground(false, false));
        button.setOnClickListener(v -> {
            selectedStyle.textGravity = gravity;
            applyStyle();
        });
        alignButtons.add(button);
        binding.alignmentOptions.addView(button);
    }

    private void applyStyle() {
        BackgroundOption background = findBackgroundOption(selectedStyle.backgroundPreset);
        binding.quoteCardSurface.setBackground(createBackgroundDrawable(background, dp(26), false));

        boolean lightText = isLightText();
        binding.patternOverlay.setPattern(selectedStyle.patternPreset, lightText);
        binding.txtQuote.setTypeface(selectedStyle.getTypeface());
        binding.txtQuoteSource.setTypeface(selectedStyle.getTypeface());
        binding.txtQuote.setTextColor(selectedStyle.textColor);
        binding.txtQuoteSource.setTextColor(selectedStyle.textColor);
        binding.txtQuote.setShadowLayer(lightText ? 1.6f : 0f, 0f, 1f, lightText ? 0x66000000 : 0x00000000);
        binding.txtQuote.setGravity(selectedStyle.textGravity);
        binding.txtQuoteSource.setGravity(selectedStyle.textGravity);
        binding.txtQuote.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        binding.txtQuoteSource.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        binding.txtQuote.setTextSize(getReadableTextSize());
        binding.txtQuoteSource.setTextSize(Math.max(12, selectedStyle.textSizeSp * 0.52f));
        binding.txtQuoteDecor.setTextColor(getQuoteDecorColor());

        updateTextSizeControls();
        updateOptionSelection();
    }

    private void updateTextSize(int textSizeSp, boolean fromSlider) {
        selectedStyle.textSizeSp = clamp(textSizeSp, TEXT_SIZE_MIN, TEXT_SIZE_MAX);
        binding.txtTextSizeError.setVisibility(View.GONE);
        if (!fromSlider) {
            binding.sliderTextSize.setProgress(selectedStyle.textSizeSp - TEXT_SIZE_MIN);
        }
        updateInputTextSize(String.valueOf(selectedStyle.textSizeSp));
        applyStyle();
    }

    private void handleTextSizeInput(String rawValue, boolean clampValue) {
        if (TextUtils.isEmpty(rawValue)) {
            binding.txtTextSizeError.setVisibility(View.VISIBLE);
            return;
        }

        try {
            int value = Integer.parseInt(rawValue);
            if (clampValue) {
                value = clamp(value, TEXT_SIZE_MIN, TEXT_SIZE_MAX);
                updateInputTextSize(String.valueOf(value));
            }
            if (value < TEXT_SIZE_MIN || value > TEXT_SIZE_MAX) {
                binding.txtTextSizeError.setVisibility(View.VISIBLE);
                return;
            }
            selectedStyle.textSizeSp = value;
            binding.txtTextSizeError.setVisibility(View.GONE);
            binding.sliderTextSize.setProgress(value - TEXT_SIZE_MIN);
            applyStyle();
        } catch (NumberFormatException exception) {
            binding.txtTextSizeError.setVisibility(View.VISIBLE);
        }
    }

    private void clampTextSizeInput() {
        String rawValue = binding.inputTextSize.getText() == null ? "" : binding.inputTextSize.getText().toString();
        if (TextUtils.isEmpty(rawValue)) {
            updateInputTextSize(String.valueOf(selectedStyle.textSizeSp));
            binding.txtTextSizeError.setVisibility(View.GONE);
            return;
        }
        handleTextSizeInput(rawValue, true);
    }

    private void updateTextSizeControls() {
        binding.sliderTextSize.setProgress(selectedStyle.textSizeSp - TEXT_SIZE_MIN);
        updateInputTextSize(String.valueOf(selectedStyle.textSizeSp));
    }

    private void updateInputTextSize(String value) {
        if (value.equals(binding.inputTextSize.getText().toString())) {
            return;
        }
        updatingTextSizeInput = true;
        binding.inputTextSize.setText(value);
        binding.inputTextSize.setSelection(binding.inputTextSize.getText().length());
        updatingTextSizeInput = false;
    }

    private void updateOptionSelection() {
        for (int i = 0; i < presetViews.size(); i++) {
            QuoteCardPreset preset = presets.get(i);
            boolean selected = selectedStyle.backgroundPreset == preset.backgroundPreset
                    && selectedStyle.patternPreset == preset.patternPreset
                    && selectedStyle.fontPreset == preset.fontPreset;
            presetViews.get(i).setBackground(createSelectorBackground(selected, false));
        }

        for (int i = 0; i < backgroundViews.size(); i++) {
            backgroundViews.get(i).setBackground(createSelectorBackground(backgroundOptions.get(i).id == selectedStyle.backgroundPreset, false));
        }

        for (int i = 0; i < patternViews.size(); i++) {
            patternViews.get(i).setBackground(createSelectorBackground(patternOptions.get(i).id == selectedStyle.patternPreset, false));
        }

        for (int i = 0; i < fontButtons.size(); i++) {
            MaterialButton button = fontButtons.get(i);
            boolean selected = fontOptions.get(i).id == selectedStyle.fontPreset;
            button.setStrokeWidth(dp(selected ? 2 : 1));
            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, selected ? R.color.primary : R.color.outline_variant)));
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, selected ? R.color.secondary_container : R.color.surface_container_lowest)));
            button.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
        }

        for (int i = 0; i < textColorViews.size(); i++) {
            TextColorOption option = textColorOptions.get(i);
            boolean selected = selectedStyle.textColor == ContextCompat.getColor(this, option.colorResId);
            textColorViews.get(i).setBackground(createColorCircleBackground(option.colorResId, selected));
            TextView check = textColorViews.get(i).findViewWithTag("check");
            if (check != null) {
                check.setVisibility(selected ? View.VISIBLE : View.GONE);
            }
        }

        for (ImageButton button : alignButtons) {
            int index = alignButtons.indexOf(button);
            int gravity = index == 0 ? Gravity.START : index == 1 ? Gravity.CENTER : Gravity.END;
            button.setBackground(createSelectorBackground(selectedStyle.textGravity == gravity, false));
        }
    }

    private View createPresetOptionView(QuoteCardPreset preset) {
        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(dp(58), dp(72));
        frameParams.setMarginEnd(dp(10));
        frame.setLayoutParams(frameParams);
        frame.setPadding(dp(5), dp(5), dp(5), dp(5));
        frame.setContentDescription(getString(R.string.quote_card_cd_preset_format, getString(preset.nameResId)));

        BackgroundOption background = findBackgroundOption(preset.backgroundPreset);
        FrameLayout thumbnail = new FrameLayout(this);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        thumbnail.setLayoutParams(thumbParams);
        thumbnail.setBackground(createBackgroundDrawable(background, dp(12), false));

        QuotePatternView patternView = new QuotePatternView(this);
        patternView.setPattern(preset.patternPreset, isLightColor(ContextCompat.getColor(this, preset.textColorResId)));
        thumbnail.addView(patternView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        thumbnail.addView(createTinyQuoteLines(ContextCompat.getColor(this, preset.textColorResId)));
        frame.addView(thumbnail);
        return frame;
    }

    private View createSwatchView(int startColorResId, int endColorResId, int width, int height) {
        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(width + dp(10), height + dp(10));
        frameParams.setMarginEnd(dp(8));
        frame.setLayoutParams(frameParams);
        frame.setPadding(dp(5), dp(5), dp(5), dp(5));

        View swatch = new View(this);
        swatch.setBackground(createBackgroundDrawable(startColorResId, endColorResId, dp(12), false));
        frame.addView(swatch, new FrameLayout.LayoutParams(width, height, Gravity.CENTER));
        return frame;
    }

    private View createPatternOptionView(PatternOption option) {
        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(56), dp(56));
        params.setMarginEnd(dp(8));
        frame.setLayoutParams(params);
        frame.setPadding(dp(5), dp(5), dp(5), dp(5));
        frame.setContentDescription(getString(R.string.quote_card_cd_pattern_format, getString(option.nameResId)));

        FrameLayout tile = new FrameLayout(this);
        tile.setBackground(createBackgroundDrawable(R.color.quote_bg_ivory_start, R.color.quote_bg_paper_end, dp(12), false));
        QuotePatternView patternView = new QuotePatternView(this);
        patternView.setPattern(option.id, false);
        tile.addView(patternView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        if (option.id == QuoteCardStyle.PATTERN_NONE) {
            TextView none = new TextView(this);
            none.setGravity(Gravity.CENTER);
            none.setText("-");
            none.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant));
            none.setTextSize(22);
            tile.addView(none, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
        frame.addView(tile, new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.CENTER));
        return frame;
    }

    private View createColorOptionView(TextColorOption option) {
        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(48), dp(48));
        params.setMarginEnd(dp(10));
        frame.setLayoutParams(params);
        frame.setBackground(createColorCircleBackground(option.colorResId, false));
        frame.setContentDescription(getString(R.string.quote_card_cd_text_color_format, getString(option.nameResId)));
        TextView check = new TextView(this);
        check.setTag("check");
        check.setGravity(Gravity.CENTER);
        check.setText("✓");
        check.setTextSize(18);
        check.setTypeface(Typeface.DEFAULT_BOLD);
        check.setTextColor(isLightColor(ContextCompat.getColor(this, option.colorResId))
                ? ContextCompat.getColor(this, R.color.quote_text_black)
                : ContextCompat.getColor(this, R.color.white));
        check.setVisibility(View.GONE);
        frame.addView(check, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return frame;
    }

    private View createTinyQuoteLines(int color) {
        LinearLayout lines = new LinearLayout(this);
        lines.setOrientation(LinearLayout.VERTICAL);
        lines.setGravity(Gravity.CENTER);
        lines.setPadding(dp(12), dp(20), dp(12), dp(12));

        int[] widths = new int[]{30, 22, 34};
        for (int width : widths) {
            View line = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(width), dp(3));
            params.setMargins(0, 0, 0, dp(5));
            line.setLayoutParams(params);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(color);
            drawable.setCornerRadius(dp(3));
            line.setAlpha(0.72f);
            line.setBackground(drawable);
            lines.addView(line);
        }
        return lines;
    }

    private GradientDrawable createBackgroundDrawable(BackgroundOption option, int cornerRadius, boolean selected) {
        return createBackgroundDrawable(option.startColorResId, option.endColorResId, cornerRadius, selected);
    }

    private GradientDrawable createBackgroundDrawable(int startColorResId, int endColorResId, int cornerRadius, boolean selected) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{ContextCompat.getColor(this, startColorResId), ContextCompat.getColor(this, endColorResId)}
        );
        drawable.setCornerRadius(cornerRadius);
        if (selected) {
            drawable.setStroke(dp(2), ContextCompat.getColor(this, R.color.primary));
        }
        return drawable;
    }

    private GradientDrawable createSelectorBackground(boolean selected, boolean colorOption) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(this, selected ? R.color.secondary_container : R.color.surface_container_lowest));
        drawable.setCornerRadius(dp(colorOption ? 999 : 14));
        drawable.setStroke(
                dp(selected ? 2 : 1),
                ContextCompat.getColor(this, selected ? R.color.primary : R.color.outline_variant)
        );
        return drawable;
    }

    private GradientDrawable createColorCircleBackground(int colorResId, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ContextCompat.getColor(this, colorResId));
        drawable.setStroke(
                dp(selected ? 3 : 1),
                ContextCompat.getColor(this, selected ? R.color.primary : R.color.outline_variant)
        );
        return drawable;
    }

    private BackgroundOption findBackgroundOption(int id) {
        for (BackgroundOption option : backgroundOptions) {
            if (option.id == id) {
                return option;
            }
        }
        return backgroundOptions.get(0);
    }

    private int getSuggestedTextColor(BackgroundOption option) {
        return ContextCompat.getColor(this, option.dark
                ? R.color.quote_card_text_light
                : R.color.quote_text_dark_brown);
    }

    private int getReadableTextSize() {
        int length = quoteText.length();
        if (length > 420) {
            return Math.max(TEXT_SIZE_MIN, selectedStyle.textSizeSp - 12);
        }
        if (length > 260) {
            return Math.max(TEXT_SIZE_MIN, selectedStyle.textSizeSp - 7);
        }
        return selectedStyle.textSizeSp;
    }

    private boolean isLightText() {
        return isLightColor(selectedStyle.textColor);
    }

    private boolean isLightColor(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return (red * 299 + green * 587 + blue * 114) / 1000 > 160;
    }

    private int getQuoteDecorColor() {
        return ContextCompat.getColor(this, isLightText()
                ? R.color.quote_card_decor_light
                : R.color.quote_card_decor_dark);
    }

    private void saveQuoteCard() {
        if (needsLegacyStoragePermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE
            );
            return;
        }

        Bitmap bitmap = null;
        try {
            bitmap = QuoteCardExporter.renderViewToBitmap(binding.quoteCardPreview);
            QuoteCardExporter.saveBitmapToGallery(this, bitmap);
            Toast.makeText(this, R.string.quote_card_save_success, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Log.e(TAG, "Cannot save quote card.", exception);
            Toast.makeText(this, R.string.quote_card_save_error, Toast.LENGTH_SHORT).show();
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    private void shareQuoteCard() {
        Bitmap bitmap = null;
        try {
            bitmap = QuoteCardExporter.renderViewToBitmap(binding.quoteCardPreview);
            Uri imageUri = QuoteCardExporter.saveBitmapToCache(this, bitmap);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setClipData(ClipData.newUri(getContentResolver(), "quote-card", imageUri));

            if (shareIntent.resolveActivity(getPackageManager()) == null) {
                Toast.makeText(this, R.string.quote_card_no_share_app, Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.quote_card_share_title)));
        } catch (Exception exception) {
            Log.e(TAG, "Cannot share quote card.", exception);
            Toast.makeText(this, R.string.quote_card_share_error, Toast.LENGTH_SHORT).show();
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    private boolean needsLegacyStoragePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_WRITE_STORAGE) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveQuoteCard();
        } else {
            Toast.makeText(this, R.string.quote_card_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private String buildQuoteSource() {
        if (!TextUtils.isEmpty(bookTitle) && !TextUtils.isEmpty(authorName)) {
            return bookTitle + " - " + authorName;
        }
        if (!TextUtils.isEmpty(bookTitle)) {
            return bookTitle;
        }
        return authorName;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class BackgroundOption {
        final int id;
        final int nameResId;
        final int startColorResId;
        final int endColorResId;
        final boolean dark;

        BackgroundOption(int id, int nameResId, int startColorResId, int endColorResId, boolean dark) {
            this.id = id;
            this.nameResId = nameResId;
            this.startColorResId = startColorResId;
            this.endColorResId = endColorResId;
            this.dark = dark;
        }
    }

    private static class PatternOption {
        final int id;
        final int nameResId;

        PatternOption(int id, int nameResId) {
            this.id = id;
            this.nameResId = nameResId;
        }
    }

    private static class FontOption {
        final int id;
        final int nameResId;
        final Typeface typeface;

        FontOption(int id, int nameResId, Typeface typeface) {
            this.id = id;
            this.nameResId = nameResId;
            this.typeface = typeface;
        }
    }

    private static class TextColorOption {
        final int colorResId;
        final int nameResId;

        TextColorOption(int colorResId, int nameResId) {
            this.colorResId = colorResId;
            this.nameResId = nameResId;
        }
    }
}
