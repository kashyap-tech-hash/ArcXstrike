package com.zalexdev.stryker.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

public class HackerLoadingView extends View {

    private final Random random = new Random();

    private Paint ringPaint;
    private Paint dashedPaint;
    private Paint glowPaint;
    private Paint textPaint;
    private Paint subtextPaint;
    private Paint barPaint;
    private Paint bracketPaint;

    private final RectF outerBounds = new RectF();
    private final RectF middleBounds = new RectF();
    private final RectF innerBounds = new RectF();

    private float outerAngle = 0f;
    private float middleAngle = 0f;
    private float radarAngle = 0f;

    private int progress = 0;
    private String statusText = "INITIALIZING CORE...";

    // Audio / signal spectrum bars
    private final float[] barHeights = new float[18];
    private final float[] targetBarHeights = new float[18];

    private boolean isRunning = true;

    public HackerLoadingView(Context context) {
        super(context);
        init();
    }

    public HackerLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HackerLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(0xFF00FF66);
        ringPaint.setStrokeWidth(3f * density);

        dashedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashedPaint.setStyle(Paint.Style.STROKE);
        dashedPaint.setColor(0xAA00E5FF);
        dashedPaint.setStrokeWidth(2f * density);
        dashedPaint.setPathEffect(new DashPathEffect(new float[]{12f * density, 8f * density}, 0));

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setColor(0x3300FF66);
        glowPaint.setStrokeWidth(8f * density);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF00FF66);
        textPaint.setTextSize(26f * scaledDensity);
        textPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(14f, 0f, 0f, 0xFF00FF66);

        subtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtextPaint.setColor(0xFF839988);
        subtextPaint.setTextSize(10f * scaledDensity);
        subtextPaint.setTypeface(Typeface.MONOSPACE);
        subtextPaint.setTextAlign(Paint.Align.CENTER);

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(0xCC00FF66);
        barPaint.setStyle(Paint.Style.FILL);

        bracketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bracketPaint.setColor(0x8000FF66);
        bracketPaint.setStyle(Paint.Style.STROKE);
        bracketPaint.setStrokeWidth(2f * density);

        for (int i = 0; i < barHeights.length; i++) {
            barHeights[i] = random.nextFloat() * 30f * density;
            targetBarHeights[i] = random.nextFloat() * 30f * density;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float density = getResources().getDisplayMetrics().density;
        float cx = w / 2f;
        float cy = (h - 40f * density) / 2f;
        float radius = Math.min(cx, cy) - 24f * density;

        outerBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);
        middleBounds.set(cx - radius * 0.82f, cy - radius * 0.82f, cx + radius * 0.82f, cy + radius * 0.82f);
        innerBounds.set(cx - radius * 0.65f, cy - radius * 0.65f, cx + radius * 0.65f, cy + radius * 0.65f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = (h - 40f * density) / 2f;

        // 1. Draw Corner HUD Brackets
        drawCornerBrackets(canvas, cx, cy, outerBounds.width() / 2f + 14f * density, density);

        // 2. Draw Outer Segmented Cyber Ring
        canvas.save();
        canvas.rotate(outerAngle, cx, cy);
        canvas.drawArc(outerBounds, 0, 70, false, ringPaint);
        canvas.drawArc(outerBounds, 90, 70, false, ringPaint);
        canvas.drawArc(outerBounds, 180, 70, false, ringPaint);
        canvas.drawArc(outerBounds, 270, 70, false, ringPaint);
        canvas.drawArc(outerBounds, 0, 360, false, glowPaint);
        canvas.restore();

        // 3. Draw Middle Dashed Cyan Ring
        canvas.save();
        canvas.rotate(-middleAngle, cx, cy);
        canvas.drawArc(middleBounds, 0, 360, false, dashedPaint);
        canvas.restore();

        // 4. Draw Center Progress Number
        String percentStr = progress + "%";
        float textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f - 4f * density;
        canvas.drawText(percentStr, cx, textY, textPaint);

        // 5. Draw Subtext
        canvas.drawText(statusText, cx, textY + 22f * density, subtextPaint);

        // 6. Draw Audio/Signal Spectrum Bars at the bottom
        drawSpectrumBars(canvas, w, h, density);

        // 7. Update animation variables
        if (isRunning) {
            outerAngle = (outerAngle + 2.5f) % 360f;
            middleAngle = (middleAngle + 3.8f) % 360f;
            radarAngle = (radarAngle + 4.5f) % 360f;

            // Animate spectrum bars smoothly
            for (int i = 0; i < barHeights.length; i++) {
                barHeights[i] += (targetBarHeights[i] - barHeights[i]) * 0.25f;
                if (Math.abs(targetBarHeights[i] - barHeights[i]) < 2f) {
                    targetBarHeights[i] = random.nextFloat() * 32f * density + 4f * density;
                }
            }

            postInvalidateDelayed(33);
        }
    }

    private void drawCornerBrackets(Canvas canvas, float cx, float cy, float radius, float density) {
        float arm = 12f * density;
        float left = cx - radius;
        float top = cy - radius;
        float right = cx + radius;
        float bottom = cy + radius;

        // Top-left
        canvas.drawLine(left, top, left + arm, top, bracketPaint);
        canvas.drawLine(left, top, left, top + arm, bracketPaint);

        // Top-right
        canvas.drawLine(right - arm, top, right, top, bracketPaint);
        canvas.drawLine(right, top, right, top + arm, bracketPaint);

        // Bottom-left
        canvas.drawLine(left, bottom, left + arm, bottom, bracketPaint);
        canvas.drawLine(left, bottom, left, bottom - arm, bracketPaint);

        // Bottom-right
        canvas.drawLine(right - arm, bottom, right, bottom, bracketPaint);
        canvas.drawLine(right, bottom, right, bottom - arm, bracketPaint);
    }

    private void drawSpectrumBars(Canvas canvas, int w, int h, float density) {
        int count = barHeights.length;
        float barWidth = 4f * density;
        float gap = 4f * density;
        float totalWidth = count * barWidth + (count - 1) * gap;
        float startX = (w - totalWidth) / 2f;
        float baseY = h - 6f * density;

        for (int i = 0; i < count; i++) {
            float x = startX + i * (barWidth + gap);
            float barHeight = barHeights[i];
            canvas.drawRect(x, baseY - barHeight, x + barWidth, baseY, barPaint);
        }
    }

    public void setProgress(int p) {
        this.progress = Math.max(0, Math.min(100, p));
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    public void setStatusText(String text) {
        this.statusText = text;
        invalidate();
    }

    public void stopAnimation() {
        isRunning = false;
    }
}
