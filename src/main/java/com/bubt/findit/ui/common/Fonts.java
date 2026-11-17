package com.bubt.findit.ui.common;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;

/**
 * Font resolution for the FindIt interface.
 *
 * <p>Modern applications need a modern typeface, but the machine that runs the
 * project may not have it. This class walks a list of preferred families and
 * falls back to the JVM logical font, so the UI never renders with a broken
 * or missing font.
 */
public final class Fonts {

    private static final String[] PREFERRED = {
            "Inter", "Segoe UI Variable Display", "Segoe UI", "SF Pro Text",
            "Roboto", "Noto Sans", "Source Sans Pro", "Ubuntu", "DejaVu Sans"
    };

    private static final Set<String> AVAILABLE = collect();

    private static String family = PREFERRED[PREFERRED.length - 1];

    static {
        for (String candidate : PREFERRED) {
            if (AVAILABLE.contains(candidate)) {
                family = candidate;
                break;
            }
        }
    }

    private Fonts() {
    }

    private static Set<String> collect() {
        Set<String> names = new HashSet<>();
        try {
            String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames();
            for (String name : installed) {
                names.add(name.toLowerCase());
            }
        } catch (Throwable ignored) {
            // Headless or restricted environment - the logical font will be used.
        }
        return names;
    }

    public static String family() {
        return family;
    }

    /** Heavy display type for the big numbers and the login headline. */
    public static Font display(int size) {
        return new Font(family, Font.BOLD, size);
    }

    /** Semi-bold headings. */
    public static Font heading(int size) {
        return new Font(family, Font.BOLD, size);
    }

    /** Regular body text. */
    public static Font body(int size) {
        return new Font(family, Font.PLAIN, size);
    }

    /** Medium weight for buttons and labels. */
    public static Font medium(int size) {
        return new Font(family, Font.BOLD, size);
    }

    /** Small caption text. */
    public static Font caption(int size) {
        return new Font(family, Font.PLAIN, size);
    }

    /** Tabular figures for counters. */
    public static Font mono(int size) {
        return new Font("JetBrains Mono", Font.PLAIN, size);
    }
}
