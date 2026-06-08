package com.example.bookspace.quote;

public class QuoteCardPreset {
    public final int nameResId;
    public final int backgroundPreset;
    public final int patternPreset;
    public final int fontPreset;
    public final int textColorResId;
    public final int textSizeSp;
    public final int textGravity;

    public QuoteCardPreset(int nameResId, int backgroundPreset, int patternPreset,
                           int fontPreset, int textColorResId, int textSizeSp, int textGravity) {
        this.nameResId = nameResId;
        this.backgroundPreset = backgroundPreset;
        this.patternPreset = patternPreset;
        this.fontPreset = fontPreset;
        this.textColorResId = textColorResId;
        this.textSizeSp = textSizeSp;
        this.textGravity = textGravity;
    }
}
