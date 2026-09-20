package com.zalexdev.stryker.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

public class MatrixRainView extends View {

    private static final String CHARACTERS = "0123456789ABCDEF010101XYZ#@%*<>{}[]/~=+_ｦｱｳｴｵｶｷｹｺｻｼｽｾｿﾀﾂﾃﾅﾆﾇﾈﾊﾋﾎﾏﾐﾑﾒﾓﾔﾕﾗﾘﾜ";
    private final Random random = new Random();

    private Paint textPaint;
    private Paint leadPaint;

    private int columnWidth = 36;
    private int fontSize = 28;
    private int[] dropPositions;
    private char[] currentChars;

    private boolean isRunning = true;
    private boolean isPaused = false;

    public MatrixRainView(Context context) {
        super(context);
        init();
    }

    public MatrixRainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MatrixRainView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fontSize = (int) (13 * getResources().getDisplayMetrics().scaledDensity);
        columnWidth = (int) (15 * getResources().getDisplayMetrics().density);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0x8000FF66);
        textPaint.setTextSize(fontSize);
        textPaint.setTypeface(Typeface.MONOSPACE);

        leadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leadPaint.setColor(0xFFE0FFE8);
        leadPaint.setTextSize(fontSize);
        leadPaint.setTypeface(Typeface.MONOSPACE);
        leadPaint.setShadowLayer(8f, 0f, 0f, 0xFF00FF66);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        int numColumns = Math.max(1, w / columnWidth);
        dropPositions = new int[numColumns];
        currentChars = new char[numColumns];

        for (int i = 0; i < numColumns; i++) {
            dropPositions[i] = random.nextInt(Math.max(1, h / fontSize));
            currentChars[i] = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dropPositions == null || isPaused) return;

        int height = getHeight();

        for (int i = 0; i < dropPositions.length; i++) {
            int x = i * columnWidth + 4;
            int y = dropPositions[i] * fontSize;

            // Draw leading glowing character
            char leadChar = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
            canvas.drawText(String.valueOf(leadChar), x, y, leadPaint);

            // Draw trailing dimmer character
            if (dropPositions[i] > 1) {
                int trailY = (dropPositions[i] - 1) * fontSize;
                canvas.drawText(String.valueOf(currentChars[i]), x, trailY, textPaint);
            }

            // Update character randomly
            if (random.nextInt(10) > 4) {
                currentChars[i] = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
            }

            // Advance drop position
            if (y > height && random.nextFloat() > 0.965f) {
                dropPositions[i] = 0;
            } else {
                dropPositions[i]++;
            }
        }

        if (isRunning && !isPaused) {
            postInvalidateDelayed(45);
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
        invalidate();
    }

    public void stop() {
        isRunning = false;
    }
}
