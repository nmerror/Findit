package com.bubt.findit.ui.common;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * The colours of the application - one place, so the whole interface stays
 * consistent.
 *
 * <p>The palette is deliberately small: three neutrals for the surfaces, one
 * accent colour for the actions and four semantic colours for the statuses.
 * Everything is flat - no gradients, no glows - because that is what a clean
 * administration screen looks like.
 */
public final class UiTheme {

    // ----------------------------------------------------------- surfaces
    /** The background of the window. */
    public static final Color WINDOW = new Color(0xF5F7FA);
    /** The background of the sidebar. */
    public static final Color SIDEBAR = new Color(0xFFFFFF);
    /** Cards and panels. */
    public static final Color SURFACE = new Color(0xFFFFFF);
    /** Inputs, table headers, quiet surfaces. */
    public static final Color SURFACE_2 = new Color(0xF1F4F8);
    /** Hovered and pressed surfaces. */
    public static final Color SURFACE_3 = new Color(0xE4EAF1);
    /** The border of a card. */
    public static final Color STROKE = new Color(0xD8DFE8);
    /** A softer border - table separators. */
    public static final Color STROKE_SOFT = new Color(0xE8EDF3);

    // -------------------------------------------------------------- text
    public static final Color TEXT = new Color(0x16202C);
    public static final Color TEXT_DIM = new Color(0x4A5768);
    public static final Color TEXT_MUTE = new Color(0x7A8798);

    // ----------------------------------------------------------- accents
    /** The colour of the primary action. */
    public static final Color ACCENT = new Color(0x0E7C9B);
    public static final Color ACCENT_DEEP = new Color(0x0A5F77);
    public static final Color VIOLET = new Color(0x6D5BE0);
    public static final Color MINT = new Color(0x0F9D6E);
    public static final Color AMBER = new Color(0xB87A05);
    public static final Color ROSE = new Color(0xD93F5C);

    /** The text on top of the accent colour. */
    public static final Color ON_ACCENT = Color.WHITE;

    /** The colour of the selected table row. */
    public static final Color SELECTION = new Color(0xDCEFF5);

    // ----------------------------------------------------------- metrics
    public static final int R_XL = 14;
    public static final int R_LG = 10;
    public static final int R_MD = 8;
    public static final int R_SM = 6;
    public static final int GAP = 14;
    public static final int PAD = 18;

    private UiTheme() {
    }

    /**
     * Installs the look and feel and the colours of the standard Swing
     * components.
     *
     * <p>Nimbus is used on purpose: it is part of every JDK and it honours the
     * colours set below, so the application looks the same on Windows, macOS
     * and Linux.
     */
    public static void apply() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // The default look and feel is good enough.
        }
        try {
            UIManager.put("Panel.background", WINDOW);
            UIManager.put("OptionPane.background", SURFACE);
            UIManager.put("OptionPane.messageForeground", TEXT);
            UIManager.put("Label.foreground", TEXT);
            UIManager.put("Label.font", Fonts.body(13));
            UIManager.put("Button.font", Fonts.medium(13));
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", TEXT);
            UIManager.put("TextField.caretForeground", ACCENT);
            UIManager.put("PasswordField.background", Color.WHITE);
            UIManager.put("PasswordField.foreground", TEXT);
            UIManager.put("PasswordField.caretForeground", ACCENT);
            UIManager.put("ScrollPane.background", SURFACE);
            UIManager.put("Viewport.background", SURFACE);
            UIManager.put("ScrollBar.width", 11);
            UIManager.put("ScrollBar.background", SURFACE);
            UIManager.put("ScrollBar.thumb", SURFACE_3);
            UIManager.put("ScrollBar.thumbShadow", SURFACE_3);
            UIManager.put("ScrollBar.thumbHighlight", STROKE);
            UIManager.put("ScrollBar.track", SURFACE);
            UIManager.put("ScrollBar.repaintTrack", Boolean.FALSE);
            UIManager.put("Table.background", SURFACE);
            UIManager.put("Table.foreground", TEXT);
            UIManager.put("Table.selectionBackground", SELECTION);
            UIManager.put("Table.selectionForeground", TEXT);
            UIManager.put("Table.gridColor", STROKE_SOFT);
            UIManager.put("Table.showGrid", Boolean.TRUE);
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
            UIManager.put("TabbedPane.background", SURFACE);
            UIManager.put("TabbedPane.foreground", TEXT);
        } catch (Throwable ignored) {
            // The look and feel is cosmetic - never fail the app over it.
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

    /** A darker version of a colour - used for coloured text on a light surface. */
    public static Color darken(Color color, float factor) {
        return new Color(
                Math.max(0, Math.min(255, (int) (color.getRed() * factor))),
                Math.max(0, Math.min(255, (int) (color.getGreen() * factor))),
                Math.max(0, Math.min(255, (int) (color.getBlue() * factor))));
    }

    /** A colour with a different alpha - used for the hover states. */
    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /** Picks a readable colour for a given status name. */
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
