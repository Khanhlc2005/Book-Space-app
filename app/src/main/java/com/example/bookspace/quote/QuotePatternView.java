package com.example.bookspace.quote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class QuotePatternView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int patternPreset = QuoteCardStyle.PATTERN_NONE;
    private boolean lightText = false;

    public QuotePatternView(Context context) {
        super(context);
    }

    public QuotePatternView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPattern(int patternPreset, boolean lightText) {
        this.patternPreset = patternPreset;
        this.lightText = lightText;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (patternPreset == QuoteCardStyle.PATTERN_PAPER) {
            drawPaper(canvas);
        } else if (patternPreset == QuoteCardStyle.PATTERN_DOTS) {
            drawDots(canvas);
        } else if (patternPreset == QuoteCardStyle.PATTERN_LINES) {
            drawLines(canvas);
        } else if (patternPreset == QuoteCardStyle.PATTERN_VIGNETTE) {
            drawVignette(canvas);
        } else if (patternPreset == QuoteCardStyle.PATTERN_LIGHT) {
            drawCornerLight(canvas);
        }
    }

    private void drawPaper(Canvas canvas) {
        paint.setShader(null);
        paint.setColor(lightText ? Color.argb(18, 255, 255, 255) : Color.argb(16, 80, 55, 35));
        float step = dp(10);
        for (float x = -getHeight(); x < getWidth(); x += step) {
            canvas.drawLine(x, 0, x + getHeight(), getHeight(), paint);
        }
    }

    private void drawDots(Canvas canvas) {
        paint.setShader(null);
        paint.setColor(lightText ? Color.argb(24, 255, 255, 255) : Color.argb(22, 60, 45, 35));
        float step = dp(18);
        float radius = dp(1.2f);
        for (float x = step; x < getWidth(); x += step) {
            for (float y = step; y < getHeight(); y += step) {
                canvas.drawCircle(x, y, radius, paint);
            }
        }
    }

    private void drawLines(Canvas canvas) {
        paint.setShader(null);
        paint.setStrokeWidth(dp(1));
        paint.setColor(lightText ? Color.argb(22, 255, 255, 255) : Color.argb(24, 70, 52, 40));
        float step = dp(28);
        for (float y = step; y < getHeight(); y += step) {
            canvas.drawLine(dp(24), y, getWidth() - dp(24), y, paint);
        }
    }

    private void drawVignette(Canvas canvas) {
        int edgeColor = lightText ? Color.argb(84, 0, 0, 0) : Color.argb(48, 60, 38, 20);
        paint.setShader(new RadialGradient(
                getWidth() / 2f,
                getHeight() / 2f,
                Math.max(getWidth(), getHeight()) * 0.72f,
                Color.TRANSPARENT,
                edgeColor,
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setShader(null);
    }

    private void drawCornerLight(Canvas canvas) {
        paint.setShader(new LinearGradient(
                0,
                0,
                getWidth(),
                getHeight(),
                Color.argb(92, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setShader(null);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
