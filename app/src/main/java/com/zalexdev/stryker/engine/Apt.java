package com.zalexdev.stryker.engine;

import java.util.ArrayList;
import java.util.List;

public final class Apt {

    public static final String INSTALLED_MARK = "__STRYKER_PKG_OK__";
    public static final String MISSING_MARK = "__STRYKER_PKG_NO__";

    private static final String OPTS =
            "-o Dpkg::Options::=--force-confdef "
                    + "-o Dpkg::Options::=--force-confold "
                    + "-o APT::Sandbox::User=root "
                    + "-o Acquire::Retries=3";

    private Apt() {
    }

    public static List<String> env() {
        ArrayList<String> c = new ArrayList<>();
        c.add("export DEBIAN_FRONTEND=noninteractive");
        c.add("export APT_LISTCHANGES_FRONTEND=none");
        c.add("export LANG=C.UTF-8 LC_ALL=C.UTF-8");
        c.add("export HOME=/root");
        c.add("export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:$PATH");
        c.add("mkdir -p /var/lib/apt/lists/partial /var/cache/apt/archives/partial /var/cache/pacman/pkg /var/lib/pacman /tmp");
        return c;
    }

    public static String update() {
        return "if command -v pacman >/dev/null 2>&1; then pacman -Sy --noconfirm; else apt-get " + OPTS + " update; fi";
    }

    public static String upgrade() {
        return "if command -v pacman >/dev/null 2>&1; then pacman -Syu --noconfirm; else apt-get " + OPTS + " -y upgrade; fi";
    }

    public static String install(String packages) {
        return "if command -v pacman >/dev/null 2>&1; then pacman -S --noconfirm --needed " + packages
                + "; else apt-get " + OPTS + " install -y --no-install-recommends " + packages + "; fi";
    }

    public static String installRecommended(String packages) {
        return "if command -v pacman >/dev/null 2>&1; then pacman -S --noconfirm --needed " + packages
                + "; else apt-get " + OPTS + " install -y " + packages + "; fi";
    }

    public static String remove(String packages) {
        return "if command -v pacman >/dev/null 2>&1; then pacman -Rns --noconfirm " + packages
                + "; else apt-get " + OPTS + " purge -y " + packages + "; apt-get " + OPTS + " autoremove -y; fi";
    }

    public static String clean() {
        return "if command -v pacman >/dev/null 2>&1; then pacman -Scc --noconfirm; else apt-get " + OPTS + " clean; fi";
    }

    public static String listInstalled() {
        return "if command -v pacman >/dev/null 2>&1; then pacman -Q 2>/dev/null; else dpkg-query -W -f='${Package}\\t${Version}\\n' 2>/dev/null; fi";
    }

    public static String search(String query) {
        return "if command -v pacman >/dev/null 2>&1; then pacman -Ss " + shellQuote(query)
                + " 2>/dev/null | head -n 400; else apt-cache search --names-only " + shellQuote(query)
                + " 2>/dev/null | head -n 400; fi";
    }

    public static String isInstalled(String pkg) {
        return "( (command -v pacman >/dev/null 2>&1 && pacman -Q " + shellQuote(pkg) + " >/dev/null 2>&1) "
                + "|| (dpkg-query -W -f='${Status}' " + shellQuote(pkg) + " 2>/dev/null | grep -q 'ok installed') ) "
                + "&& echo " + INSTALLED_MARK + " || echo " + MISSING_MARK;
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
