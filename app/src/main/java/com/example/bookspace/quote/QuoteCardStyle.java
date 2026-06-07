package com.example.bookspace.quote;

import android.graphics.Typeface;
import android.view.Gravity;

public class QuoteCardStyle {
    public static final int BACKGROUND_IVORY = 0;
    public static final int BACKGROUND_PAPER = 1;
    public static final int BACKGROUND_BEIGE = 2;
    public static final int BACKGROUND_LIGHT_BROWN = 3;
    public static final int BACKGROUND_LIGHT_GRAY = 4;
    public static final int BACKGROUND_DARK_GRAY = 5;
    public static final int BACKGROUND_BLACK = 6;
    public static final int BACKGROUND_NAVY = 7;
    public static final int BACKGROUND_DEEP_GREEN = 8;
    public static final int BACKGROUND_DEEP_PURPLE = 9;
    public static final int BACKGROUND_SOFT_PINK = 10;
    public static final int BACKGROUND_WARM_GRADIENT = 11;

    public static final int PATTERN_NONE = 0;
    public static final int PATTERN_PAPER = 1;
    public static final int PATTERN_DOTS = 2;
    public static final int PATTERN_LINES = 3;
    public static final int PATTERN_VIGNETTE = 4;
    public static final int PATTERN_LIGHT = 5;

    public static final int FONT_SANS = 0;
    public static final int FONT_SERIF = 1;
    public static final int FONT_MONO = 2;
    public static final int FONT_BOLD = 3;
    public static final int FONT_CLASSIC = 4;

    public int backgroundPreset = BACKGROUND_PAPER;
    public int patternPreset = PATTERN_PAPER;
    public int fontPreset = FONT_SERIF;
    public int textSizeSp = 24;
    public int textColor;
    public int textGravity = Gravity.CENTER;
    public boolean textColorManuallyChanged = false;

    public QuoteCardStyle(int defaultTextColor) {
        textColor = defaultTextColor;
    }

    public Typeface getTypeface() {
        if (fontPreset == FONT_MONO) {
            return Typeface.MONOSPACE;
        }
        if (fontPreset == FONT_BOLD) {
            return Typeface.DEFAULT_BOLD;
        }
        if (fontPreset == FONT_SANS) {
            return Typeface.SANS_SERIF;
        }
        if (fontPreset == FONT_CLASSIC) {
            return Typeface.create(Typeface.SERIF, Typeface.ITALIC);
        }
        return Typeface.SERIF;
    }
}
