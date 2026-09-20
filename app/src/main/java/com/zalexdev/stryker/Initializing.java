package com.zalexdev.stryker;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.custom.HackerLoadingView;
import com.zalexdev.stryker.custom.MatrixRainView;

import java.util.Random;

public class Initializing extends Fragment {

    public static final long BOOT_DURATION_MS = 5500; // > 5 seconds hacker boot animation

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private MatrixRainView matrixRain;
    private TextView hexTicker;
    private TextView badgeStatus;
    private HackerLoadingView hackerLoadingHud;
    private TextView brandTitle;
    private TextView bootStageText;
    private TextView terminalText;
    private ScrollView terminalScroll;
    private LinearProgressIndicator bootProgress;
    private TextView bootStatusLine;

    private boolean isDestroyed = false;
    private int currentLogIndex = 0;
    private final StringBuilder logBuilder = new StringBuilder();

    private static final String[] HACKER_BOOT_LOGS = new String[]{
            ">> [0.050s] [HYPERVISOR] Arch Linux 6.8.9-arcx aarch64 initializing...",
            ">> [0.520s] [VIRT_KVM] Slirp virtual network interface up (10.0.2.15)",
            ">> [0.980s] [SELINUX] Enforcing policy disabled -> Knox container bypassed",
            ">> [1.440s] [STORAGE] Mounting virtfs /sdcard/Stryker -> OverlayFS linked",
            ">> [1.920s] [SANDBOX] Rootless sandbox environment initialized [UID 1000]",
            ">> [2.400s] [NETWORK] Packet injection raw driver armed -> wlan0mon ready",
            ">> [2.880s] [EXPLOIT_DB] CVE database mirror synced (62,419 local exploits)",
            ">> [3.360s] [NUCLEI] Vulnerability scanner signatures loaded (8,420 YAMLs)",
            ">> [3.840s] [METASPLOIT] Framework RPC daemon handshake established [Port 55553]",
            ">> [4.320s] [SUBSYSTEM] BadUSB & HID configfs gadget endpoints mapped",
            ">> [4.800s] [KEYSTORE] Crypto vault unlocked // AES-256-GCM cipher active",
            ">> [5.250s] [SYSTEM_READY] All offensive vectors armed // Stryker online"
    };

    private static final String[] HEX_SAMPLES = new String[]{
            "0x7F454C46", "0x00FF66AA", "0xDEADBEEF", "0xCAFEBABE",
            "0x89504E47", "0x00E67600", "0x1337C0DE", "0xC001D00D",
            "0xBAADF00D", "0xFEEDFACE"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.init, container, false);

        matrixRain = view.findViewById(R.id.init_matrix_rain);
        hexTicker = view.findViewById(R.id.hex_ticker);
        badgeStatus = view.findViewById(R.id.badge_status);
        hackerLoadingHud = view.findViewById(R.id.hacker_loading_hud);
        brandTitle = view.findViewById(R.id.brand_title);
        bootStageText = view.findViewById(R.id.boot_stage_text);
        terminalText = view.findViewById(R.id.terminal_text);
        terminalScroll = view.findViewById(R.id.terminal_scroll);
        bootProgress = view.findViewById(R.id.boot_progress);
        bootStatusLine = view.findViewById(R.id.boot_status_line);

        // Tap anywhere to request immediate bypass
        view.findViewById(R.id.init_root).setOnClickListener(v -> triggerBypass());
        View bypassHint = view.findViewById(R.id.bypass_hint);
        if (bypassHint != null) {
            bypassHint.setOnClickListener(v -> triggerBypass());
        }

        startGlitchLoop();
        startHexTickerLoop();
        startTerminalTypewriter();
        startBootProgressAnimation();

        return view;
    }

    private void triggerBypass() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).bypassBootAnimation();
        }
    }

    private void startGlitchLoop() {
        if (isDestroyed) return;

        boolean shouldGlitch = random.nextInt(10) > 6;
        if (shouldGlitch && brandTitle != null) {
            float jitterX = (random.nextFloat() - 0.5f) * 12f;
            float jitterY = (random.nextFloat() - 0.5f) * 4f;
            float alphaJitter = 0.75f + random.nextFloat() * 0.25f;

            brandTitle.setTranslationX(jitterX);
            brandTitle.setTranslationY(jitterY);
            brandTitle.setAlpha(alphaJitter);

            handler.postDelayed(() -> {
                if (brandTitle != null) {
                    brandTitle.setTranslationX(0f);
                    brandTitle.setTranslationY(0f);
                    brandTitle.setAlpha(1.0f);
                }
            }, 60);
        }

        handler.postDelayed(this::startGlitchLoop, 140 + random.nextInt(140));
    }

    private void startHexTickerLoop() {
        if (isDestroyed) return;
        if (hexTicker != null) {
            String hex = HEX_SAMPLES[random.nextInt(HEX_SAMPLES.length)];
            hexTicker.setText(hex);
        }
        handler.postDelayed(this::startHexTickerLoop, 160);
    }

    private void startTerminalTypewriter() {
        if (isDestroyed) return;
        if (currentLogIndex < HACKER_BOOT_LOGS.length) {
            String nextLine = HACKER_BOOT_LOGS[currentLogIndex];
            if (logBuilder.length() > 0) {
                logBuilder.append("\n");
            }
            logBuilder.append(nextLine);
            if (terminalText != null) {
                terminalText.setText(logBuilder.toString());
            }
            if (bootStatusLine != null) {
                bootStatusLine.setText(nextLine.replace(">> ", ""));
            }
            if (terminalScroll != null) {
                terminalScroll.post(() -> terminalScroll.fullScroll(View.FOCUS_DOWN));
            }

            // Update stages based on progress
            updateStage(currentLogIndex);

            currentLogIndex++;

            if (currentLogIndex == HACKER_BOOT_LOGS.length) {
                if (badgeStatus != null) {
                    badgeStatus.setText("ARMED");
                    badgeStatus.setTextColor(0xFF00FF66);
                }
            }

            // Schedule next log across the >5s duration (~440ms per line)
            handler.postDelayed(this::startTerminalTypewriter, 440);
        }
    }

    private void updateStage(int index) {
        if (bootStageText == null || hackerLoadingHud == null) return;
        if (index <= 2) {
            bootStageText.setText("[ STAGE 1/5: HYPERVISOR INITIALIZATION ]");
            hackerLoadingHud.setStatusText("HYPERVISOR INIT");
        } else if (index <= 4) {
            bootStageText.setText("[ STAGE 2/5: MOUNTING VIRTFS & SANDBOX ]");
            hackerLoadingHud.setStatusText("VIRTFS MOUNT");
        } else if (index <= 6) {
            bootStageText.setText("[ STAGE 3/5: SYNCING CVE EXPLOIT ARCHIVES ]");
            hackerLoadingHud.setStatusText("EXPLOIT-DB SYNC");
        } else if (index <= 8) {
            bootStageText.setText("[ STAGE 4/5: INITIALIZING OFFENSIVE FRAMEWORKS ]");
            hackerLoadingHud.setStatusText("FRAMEWORKS INIT");
        } else {
            bootStageText.setText("[ STAGE 5/5: ALL SYSTEMS OPERATIONAL ]");
            hackerLoadingHud.setStatusText("ONLINE // ARMED");
        }
    }

    private void startBootProgressAnimation() {
        ValueAnimator progressAnim = ValueAnimator.ofInt(0, 100);
        progressAnim.setDuration(BOOT_DURATION_MS);
        progressAnim.setInterpolator(new LinearInterpolator());
        progressAnim.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            if (bootProgress != null) {
                bootProgress.setProgress(progress);
            }
            if (hackerLoadingHud != null) {
                hackerLoadingHud.setProgress(progress);
            }
        });
        progressAnim.start();
    }

    @Override
    public void onDestroyView() {
        isDestroyed = true;
        if (matrixRain != null) matrixRain.stop();
        if (hackerLoadingHud != null) hackerLoadingHud.stopAnimation();
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}