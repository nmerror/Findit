package com.bubt.findit.ui.common;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * The FindIt design system - one place for every colour, radius and font.
 *
 * <p>Look: dark "deep space" surfaces, luminous cyan accents, thin luminous
 * strokes and generous spacing. Nothing in the UI uses the default Swing
 * look-and-feel widgets, so every pixel is under our control.
 *
 * <p><b>Two modes.</b> The palette is live: {@link #setMode(Mode)} swaps every
 * colour at runtime and the screen is rebuilt around the new palette, so the
 * whole application can be switched between dark and light without a restart.
 */
public final class UiTheme {

    /** The two palettes of the application. */
    public enum Mode {
        DARK("Dark"), LIGHT("Light");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private static Mode mode = Mode.DARK;

    // ----------------------------------------------------------- surfaces
    public static Color WINDOW;
    public static Color WINDOW_DEEP;
    public static Color SIDEBAR;
    public static Color SURFACE;
    public static Color SURFACE_2;
    public static Color SURFACE_3;
    public static Color STROKE;
    public static Color STROKE_SOFT;

    // -------------------------------------------------------------- text
    public static Color TEXT;
    public static Color TEXT_DIM;
    public static Color TEXT_MUTE;

    // ----------------------------------------------------------- accents
    public static Color ACCENT;
    public static Color ACCENT_DEEP;
    public static Color VIOLET;
    public static Color MINT;
    public static Color AMBER;
    public static Color ROSE;

    /** Text drawn on top of a filled accent surface. */
    public static Color ON_ACCENT;

    /** Row selection colour of the tables. */
    public static Color SELECTION;

    /** Colour of the faint grid drawn behind the login hero. */
    public static Color GRID;

    /** Top colour of the gradient used on the sign-in card. */
    public static Color CARD_GRADIENT;

    // ----------------------------------------------------------- metrics
    public static final int R_XL = 22;
    public static final int R_LG = 16;
    public static final int R_MD = 11;
    public static final int R_SM = 7;
    public static final int GAP = 16;
    public static final int PAD = 22;

    static {
        applyMode(Mode.DARK);
    }

    private UiTheme() {
    }

    /** Switches the palette and notifies the Swing look and feel. */
    public static void setMode(Mode newMode) {
        mode = newMode == null ? Mode.DARK : newMode;
        applyMode(mode);
        apply();
    }

    /** Flips between dark and light. */
    public static void toggleMode() {
        setMode(mode == Mode.DARK ? Mode.LIGHT : Mode.DARK);
    }

    public static Mode mode() {
        return mode;
    }

    public static boolean isDark() {
        return mode == Mode.DARK;
    }

    private static void applyMode(Mode target) {
        if (target == Mode.LIGHT) {
            WINDOW = new Color(0xEDF1F7);
            WINDOW_DEEP = new Color(0xE1E8F1);
            SIDEBAR = new Color(0xFFFFFF);
            SURFACE = new Color(0xFFFFFF);
            SURFACE_2 = new Color(0xF1F5FA);
            SURFACE_3 = new Color(0xE4EAF2);
            STROKE = new Color(0xD2DCE8);
            STROKE_SOFT = new Color(0xE7EDF4);

            TEXT = new Color(0x0F1B2A);
            TEXT_DIM = new Color(0x4A5B72);
            TEXT_MUTE = new Color(0x7C8CA1);

            ACCENT = new Color(0x0E93B5);
            ACCENT_DEEP = new Color(0x0A6C89);
            VIOLET = new Color(0x6D5BE0);
            MINT = new Color(0x0F9D6E);
            AMBER = new Color(0xB87A05);
            ROSE = new Color(0xD93F5C);

            ON_ACCENT = Color.WHITE;
            SELECTION = new Color(0xD6EEF6);
            GRID = new Color(0x0F1B2A);
            CARD_GRADIENT = new Color(0xFBFDFF);
        } else {
            WINDOW = new Color(0x070C15);
            WINDOW_DEEP = new Color(0x04070D);
            SIDEBAR = new Color(0x0A1220);
            SURFACE = new Color(0x101B2D);
            SURFACE_2 = new Color(0x16233A);
            SURFACE_3 = new Color(0x1D2C46);
            STROKE = new Color(0x22334F);
            STROKE_SOFT = new Color(0x18263C);

            TEXT = new Color(0xE9F0FA);
            TEXT_DIM = new Color(0x93A7C2);
            TEXT_MUTE = new Color(0x64789A);

            ACCENT = new Color(0x25D0F0);
            ACCENT_DEEP = new Color(0x0E8FB0);
            VIOLET = new Color(0x8B7CFF);
            MINT = new Color(0x36E0A5);
            AMBER = new Color(0xFFC24B);
            ROSE = new Color(0xFF7A93);

            ON_ACCENT = new Color(0x050A12);
            SELECTION = new Color(0x1B3A55);
            GRID = Color.WHITE;
            CARD_GRADIENT = new Color(0x16243A);
        }
    }

    /** Applies the palette to the Swing components we do not paint ourselves. */
    public static void apply() {
        try {
            UIManager.put("Panel.background", WINDOW);
            UIManager.put("OptionPane.background", SURFACE);
            UIManager.put("OptionPane.messageForeground", TEXT);
            UIManager.put("Label.foreground", TEXT);
            UIManager.put("Label.font", Fonts.body(13));
            UIManager.put("Button.font", Fonts.medium(13));
            UIManager.put("TextField.background", SURFACE_2);
            UIManager.put("TextField.foreground", TEXT);
            UIManager.put("TextField.caretForeground", ACCENT);
            UIManager.put("PasswordField.background", SURFACE_2);
            UIManager.put("PasswordField.foreground", TEXT);
            UIManager.put("PasswordField.caretForeground", ACCENT);
            UIManager.put("ScrollPane.background", SURFACE);
            UIManager.put("Viewport.background", SURFACE);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.background", WINDOW);
            UIManager.put("ScrollBar.thumb", SURFACE_3);
            UIManager.put("ScrollBar.thumbShadow", SURFACE_3);
            UIManager.put("ScrollBar.thumbHighlight", STROKE);
            UIManager.put("ScrollBar.track", WINDOW);
            UIManager.put("ScrollBar.repaintTrack", Boolean.FALSE);
            UIManager.put("Table.background", SURFACE);
            UIManager.put("Table.foreground", TEXT);
            UIManager.put("Table.selectionBackground", SELECTION);
            UIManager.put("Table.selectionForeground", TEXT);
            UIManager.put("Table.gridColor", STROKE_SOFT);
            UIManager.put("Table.showGrid", Boolean.FALSE);
            UIManager.put("TableHeader.background", SURFACE_2);
            UIManager.put("TableHeader.foreground", TEXT_MUTE);
            UIManager.put("TableHeader.font", Fonts.caption(11));
            UIManager.put("ToolTip.background", SURFACE_3);
            UIManager.put("ToolTip.foreground", TEXT);
            UIManager.put("ToolTip.font", Fonts.caption(12));
            UIManager.put("ProgressBar.background", SURFACE_2);
            UIManager.put("ProgressBar.foreground", ACCENT);
            UIManager.put("ToggleButton.background", SURFACE_2);
            UIManager.put("CheckBox.background", SURFACE);
            UIManager.put("CheckBox.foreground", TEXT);
        } catch (Throwable ignored) {
            // UIManager keys are cosmetic - never fail the app over them.
        }
    }

    // ---------------------------------------------------------- factories

    public static Font display(int size) {
        return Fonts.display(size);
    }

    public static Font heading(int size) {
        return Fonts.heading(size);
    }

    public static Font body(int size) {
        return Fonts.body(size);
    }

    public static Font caption(int size) {
        return Fonts.caption(size);
    }

    /** Mixes two colours - used for gradients and glows. */
    public static Color mix(Color a, Color b, double ratio) {
        double t = Math.max(0, Math.min(1, ratio));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (int) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    /** A colour with a different alpha - used for glows and hovers. */
    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /** Picks a readable accent for a given status name. */
    public static Color accentFor(String status) {
        if (status == null) {
            return TEXT_DIM;
        }
        return switch (status.toUpperCase()) {
            case "ACTIVE", "OPEN", "APPROVED", "RETURNED", "CONFIRMED" -> MINT;
            case "SUSPENDED", "REJECTED", "CLOSED" -> ROSE;
            case "PENDING", "SUBMITTED", "UNDER_REVIEW", "NEW", "VIEWED" -> AMBER;
            default -> TEXT_DIM;
        };
    }
}
