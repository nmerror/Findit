package com.bubt.findit.ui.common;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;

/**
 * Font resolution for the FindIt interface.
 *
 * <p>The application uses the standard user interface font of the machine. If
 * one of the nicer families is installed it is used, otherwise the logical
 * font {@code SansSerif} of Java takes over - that one always resolves to the
 * font the operating system uses for its own windows.
 */
public final class Fonts {

    private static final String[] PREFERRED = {
            "Inter", "Segoe UI", "SF Pro Text", "Roboto", "Noto Sans",
            "Helvetica Neue", "Liberation Sans", "DejaVu Sans"
    };

    private static final Set<String> AVAILABLE = collect();

    private static final String FAMILY = resolve();

    private Fonts() {
    }

    private static String resolve() {
        for (String candidate : PREFERRED) {
            if (AVAILABLE.contains(candidate.toLowerCase())) {
                return candidate;
            }
        }
        return Font.SANS_SERIF;
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
        return FAMILY;
    }

    /** Heavy display type for the big numbers and the login headline. */
    public static Font display(int size) {
        return new Font(FAMILY, Font.BOLD, size);
    }

    /** Bold type for headings. */
    public static Font heading(int size) {
        return new Font(FAMILY, Font.BOLD, size);
    }

    /** Regular body text. */
    public static Font body(int size) {
        return new Font(FAMILY, Font.PLAIN, size);
    }

    /** Regular weight for labels, buttons and navigation entries. */
    public static Font medium(int size) {
        return new Font(FAMILY, Font.PLAIN, size);
    }

    /** Small caption text. */
    public static Font caption(int size) {
        return new Font(FAMILY, Font.PLAIN, size);
    }

    /** Tabular figures for counters. */
    public static Font mono(int size) {
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }
}
