package com.zalexdev.stryker.landing;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zalexdev.stryker.MainActivity;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.FingerprintScannerView;
import com.zalexdev.stryker.custom.MatrixRainView;

import java.util.Random;

public class LandingFragment extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private MatrixRainView matrixRain;
    private View statusDot;
    private TextView hexTicker;
    private TextView landingTitle;
    private FingerprintScannerView fingerprintScanner;
    private TextView biometricStatusTitle;
    private TextView biometricHint;
    private LinearLayout startButtonBar;

    private boolean isDestroyed = false;
    private boolean isLaunching = false;

    private static final String[] HEX_SAMPLES = new String[]{
            "0x7F454C46", "0x00FF66AA", "0xDEADBEEF", "0xCAFEBABE",
            "0x89504E47", "0x00E67600", "0x1337C0DE", "0xC001D00D",
            "0xBAADF00D", "0xFEEDFACE"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        matrixRain = view.findViewById(R.id.matrix_rain_overlay);
        statusDot = view.findViewById(R.id.landing_status_dot);
        hexTicker = view.findViewById(R.id.landing_hex_ticker);
        landingTitle = view.findViewById(R.id.landing_title);
        fingerprintScanner = view.findViewById(R.id.fingerprint_scanner);
        biometricStatusTitle = view.findViewById(R.id.biometric_status_title);
        biometricHint = view.findViewById(R.id.biometric_hint);
        startButtonBar = view.findViewById(R.id.start_button_bar);

        startDotPulse();
        startHexTickerLoop();
        startGlitchLoop();
        setupFingerprintAndStart();

        return view;
    }

    private void startDotPulse() {
        if (statusDot == null) return;
        ObjectAnimator pulse = ObjectAnimator.ofFloat(statusDot, "alpha", 0.3f, 1.0f);
        pulse.setDuration(800);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.start();
    }

    private void startHexTickerLoop() {
        if (isDestroyed) return;
        if (hexTicker != null) {
            String hex = HEX_SAMPLES[random.nextInt(HEX_SAMPLES.length)];
            hexTicker.setText(hex);
        }
        handler.postDelayed(this::startHexTickerLoop, 180);
    }

    private void startGlitchLoop() {
        if (isDestroyed) return;
        if (landingTitle != null && random.nextInt(10) > 6) {
            float jitterX = (random.nextFloat() - 0.5f) * 10f;
            float jitterY = (random.nextFloat() - 0.5f) * 4f;
            float alphaJitter = 0.8f + random.nextFloat() * 0.2f;

            landingTitle.setTranslationX(jitterX);
            landingTitle.setTranslationY(jitterY);
            landingTitle.setAlpha(alphaJitter);

            handler.postDelayed(() -> {
                if (landingTitle != null) {
                    landingTitle.setTranslationX(0f);
                    landingTitle.setTranslationY(0f);
                    landingTitle.setAlpha(1.0f);
                }
            }, 60);
        }
        handler.postDelayed(this::startGlitchLoop, 150 + random.nextInt(150));
    }

    private void setupFingerprintAndStart() {
        if (fingerprintScanner != null) {
            fingerprintScanner.setOnScanCompleteListener(new FingerprintScannerView.OnScanCompleteListener() {
                @Override
                public void onScanStarted() {
                    if (biometricStatusTitle != null) {
                        biometricStatusTitle.setText("SCANNING BIOMETRIC SIGNATURE...");
                        biometricStatusTitle.setTextColor(0xFF00E5FF);
                    }
                    if (biometricHint != null) {
                        biometricHint.setText("[ VERIFYING CRYPTOGRAPHIC TOKEN ]");
                    }
                }

                @Override
                public void onScanProgress(int percent) {
                    if (biometricStatusTitle != null) {
                        biometricStatusTitle.setText("SCANNING: " + percent + "%");
                    }
                }

                @Override
                public void onScanComplete() {
                    if (isLaunching) return;
                    isLaunching = true;

                    if (biometricStatusTitle != null) {
                        biometricStatusTitle.setText("ACCESS GRANTED // OVERRIDE ACCEPTED");
                        biometricStatusTitle.setTextColor(0xFF00FF66);
                    }
                    if (biometricHint != null) {
                        biometricHint.setText("[ INITIALIZING ARCXSTRIKE ENVIRONMENT ]");
                    }

                    handler.postDelayed(() -> {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).startHackerBootSequence();
                        }
                    }, 400);
                }
            });
        }

        if (startButtonBar != null) {
            startButtonBar.setOnClickListener(v -> {
                if (fingerprintScanner != null && !isLaunching) {
                    fingerprintScanner.startScanning();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        isDestroyed = true;
        if (matrixRain != null) matrixRain.stop();
        if (fingerprintScanner != null) fingerprintScanner.stopAnimation();
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}
