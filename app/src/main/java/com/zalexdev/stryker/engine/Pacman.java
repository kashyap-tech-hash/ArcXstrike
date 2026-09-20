package com.zalexdev.stryker.engine;

import java.util.ArrayList;
import java.util.List;

public final class Pacman {

    public static final String INSTALLED_MARK = "__ARCX_PKG_OK__";
    public static final String MISSING_MARK = "__ARCX_PKG_NO__";

    private static final String OPTS = "--noconfirm --needed";

    private Pacman() {
    }

    public static List<String> env() {
        ArrayList<String> c = new ArrayList<>();
        c.add("export LANG=C.UTF-8 LC_ALL=C.UTF-8");
        c.add("export HOME=/root");
        c.add("export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:$PATH");
        c.add("mkdir -p /var/cache/pacman/pkg /var/lib/pacman /tmp");
        return c;
    }

    public static String update() {
        return "pacman -Sy --noconfirm";
    }

    public static String upgrade() {
        return "pacman -Syu --noconfirm";
    }

    public static String install(String packages) {
        return "pacman -S " + OPTS + " " + packages;
    }

    public static String installRecommended(String packages) {
        return "pacman -S " + OPTS + " " + packages;
    }

    public static String remove(String packages) {
        return "pacman -Rns --noconfirm " + packages;
    }

    public static String clean() {
        return "pacman -Scc --noconfirm";
    }

    public static String listInstalled() {
        return "pacman -Q 2>/dev/null";
    }

    public static String search(String query) {
        return "pacman -Ss " + shellQuote(query) + " 2>/dev/null | head -n 400";
    }

    public static String isInstalled(String pkg) {
        return "pacman -Q " + shellQuote(pkg) + " >/dev/null 2>&1 && echo " + INSTALLED_MARK + " || echo " + MISSING_MARK;
    }

    public static String hasBinary(String bin) {
        return "command -v " + shellQuote(bin) + " >/dev/null 2>&1 && echo " + INSTALLED_MARK
                + " || echo " + MISSING_MARK;
    }

    public static String shellQuote(String raw) {
        if (raw == null) return "''";
        return "'" + raw.replace("'", "'\\''") + "'";
    }
}
