package com.zalexdev.stryker.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.zalexdev.stryker.R;

public class FingerprintScannerView extends View {

    public interface OnScanCompleteListener {
        void onScanStarted();
        void onScanProgress(int percent);
        void onScanComplete();
    }

    private Paint ringPaint;
    private Paint ripplePaint;
    private Paint laserPaint;
    private Paint glowPaint;
    private Drawable fingerprintDrawable;

    private final RectF fingerprintBounds = new RectF();
    private float laserY = 0f;
    private boolean laserGoingDown = true;
    private float laserSpeed = 3.5f;

    private float rippleRadius1 = 0f;
    private float rippleRadius2 = 0f;
    private float maxRippleRadius = 0f;
    private float reticleAngle = 0f;

    public static final int STATE_IDLE = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_AUTHENTICATED = 2;
    private int currentState = STATE_IDLE;

    private int scanProgress = 0;
    private OnScanCompleteListener scanListener;
    private Vibrator vibrator;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAnimating = true;

    public FingerprintScannerView(Context context) {
        super(context);
        init();
    }

    public FingerprintScannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FingerprintScannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(0xFF00FF66);
        ringPaint.setStrokeWidth(2.5f * density);

        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setColor(0x8000FF66);
        ripplePaint.setStrokeWidth(2f * density);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setColor(0x4000FF66);
        glowPaint.setStrokeWidth(8f * density);

        laserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        laserPaint.setStyle(Paint.Style.FILL);

        fingerprintDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_fingerprint_cyber);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float density = getResources().getDisplayMetrics().density;
        float cx = w / 2f;
        float cy = h / 2f;
        float iconSize = 72f * density;

        fingerprintBounds.set(cx - iconSize / 2f, cy - iconSize / 2f, cx + iconSize / 2f, cy + iconSize / 2f);
        if (fingerprintDrawable != null) {
            fingerprintDrawable.setBounds(
                    (int) fingerprintBounds.left,
                    (int) fingerprintBounds.top,
                    (int) fingerprintBounds.right,
                    (int) fingerprintBounds.bottom
            );
        }

        laserY = fingerprintBounds.top;
        maxRippleRadius = Math.min(cx, cy) - 4f * density;
        rippleRadius1 = iconSize / 2f + 4f * density;
        rippleRadius2 = rippleRadius1 + 18f * density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // 1. Draw Expanding Ripples
        drawRipples(canvas, cx, cy);

        // 2. Draw Rotating Outer Reticle Ring
        canvas.save();
        canvas.rotate(reticleAngle, cx, cy);
        float baseRadius = fingerprintBounds.width() / 2f + 16f * density;
        RectF ringBounds = new RectF(cx - baseRadius, cy - baseRadius, cx + baseRadius, cy + baseRadius);
        canvas.drawArc(ringBounds, 15, 60, false, ringPaint);
        canvas.drawArc(ringBounds, 105, 60, false, ringPaint);
        canvas.drawArc(ringBounds, 195, 60, false, ringPaint);
        canvas.drawArc(ringBounds, 285, 60, false, ringPaint);
        canvas.restore();

        // 3. Draw Center Fingerprint Icon
        if (fingerprintDrawable != null) {
            fingerprintDrawable.draw(canvas);
        }

        // 4. Draw Animated Laser Beam
        drawLaserBeam(canvas, density);

        // 5. Update Animations
        if (isAnimating) {
            updateAnimation(density);
            postInvalidateDelayed(25);
        }
    }

    private void drawRipples(Canvas canvas, float cx, float cy) {
        if (maxRippleRadius <= 0) return;

        // Ripple 1
        float fraction1 = rippleRadius1 / maxRippleRadius;
        int alpha1 = (int) ((1f - Math.min(1f, fraction1)) * 160);
        ripplePaint.setAlpha(Math.max(0, alpha1));
        canvas.drawCircle(cx, cy, rippleRadius1, ripplePaint);

        // Ripple 2
        float fraction2 = rippleRadius2 / maxRippleRadius;
        int alpha2 = (int) ((1f - Math.min(1f, fraction2)) * 160);
        ripplePaint.setAlpha(Math.max(0, alpha2));
        canvas.drawCircle(cx, cy, rippleRadius2, ripplePaint);
    }

    private void drawLaserBeam(Canvas canvas, float density) {
        float beamHeight = 6f * density;
        float left = fingerprintBounds.left - 8f * density;
        float right = fingerprintBounds.right + 8f * density;
        float top = laserY - beamHeight / 2f;
        float bottom = laserY + beamHeight / 2f;

        int beamColor = (currentState == STATE_SCANNING) ? 0xFF00E5FF : 0xFF00FF66;
        int centerColor = (currentState == STATE_AUTHENTICATED) ? 0xFFFFFFFF : beamColor;

        LinearGradient gradient = new LinearGradient(
                left, laserY, right, laserY,
                new int[]{0x0000FF66, centerColor, 0x0000FF66},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );
        laserPaint.setShader(gradient);
        canvas.drawRect(left, top, right, bottom, laserPaint);
    }

    private void updateAnimation(float density) {
        reticleAngle = (reticleAngle + (currentState == STATE_SCANNING ? 5f : 1.5f)) % 360f;

        // Laser scan movement
        float step = laserSpeed * (currentState == STATE_SCANNING ? 2.5f : 1f);
        if (laserGoingDown) {
            laserY += step;
            if (laserY >= fingerprintBounds.bottom) {
                laserY = fingerprintBounds.bottom;
                laserGoingDown = false;
            }
        } else {
            laserY -= step;
            if (laserY <= fingerprintBounds.top) {
                laserY = fingerprintBounds.top;
                laserGoingDown = true;
            }
        }

        // Ripple expansion
        float rippleSpeed = (currentState == STATE_SCANNING ? 3.5f : 1.2f) * density;
        float minRadius = fingerprintBounds.width() / 2f + 4f * density;

        rippleRadius1 += rippleSpeed;
        if (rippleRadius1 > maxRippleRadius) {
            rippleRadius1 = minRadius;
        }

        rippleRadius2 += rippleSpeed;
        if (rippleRadius2 > maxRippleRadius) {
            rippleRadius2 = minRadius;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentState == STATE_AUTHENTICATED) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startScanning();
                return true;
            case MotionEvent.ACTION_UP:
                if (currentState == STATE_SCANNING && scanProgress < 100) {
                    // Tap completed -> proceed with smooth scan to completion
                    // (Allowing both quick tap and hold to trigger authentication smoothly)
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void startScanning() {
        if (currentState == STATE_SCANNING || currentState == STATE_AUTHENTICATED) return;

        currentState = STATE_SCANNING;
        scanProgress = 0;
        triggerHapticFeedback();

        if (scanListener != null) {
            scanListener.onScanStarted();
        }

        // Run authentic biometric scan progression
        ValueAnimator scanAnim = ValueAnimator.ofInt(0, 100);
        scanAnim.setDuration(950);
        scanAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scanAnim.addUpdateListener(anim -> {
            scanProgress = (int) anim.getAnimatedValue();
            if (scanListener != null) {
                scanListener.onScanProgress(scanProgress);
            }
            invalidate();
        });
        scanAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                currentState = STATE_AUTHENTICATED;
                triggerSuccessHaptic();
                invalidate();
                if (scanListener != null) {
                    scanListener.onScanComplete();
                }
            }
        });
        scanAnim.start();
    }

    private void triggerHapticFeedback() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(40);
            }
        }
    }

    private void triggerSuccessHaptic() {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 60, 50, 90};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    public void setOnScanCompleteListener(OnScanCompleteListener listener) {
        this.scanListener = listener;
    }

    public void stopAnimation() {
        isAnimating = false;
    }
}
