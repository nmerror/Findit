package com.bubt.findit.ui.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared painting logic for the input fields: a plain surface, a thin border
 * that turns into the accent colour while the field has the focus, and a
 * placeholder that disappears while the user types.
 *
 * <p>The surface and the border are painted before the text of Swing, the
 * placeholder afterwards - Swing paints the text field editor on top of it.
 */
final class FieldSkin {

    private FieldSkin() {
    }

    /** The surface and the border of the field. */
    static void paint(Graphics g, int width, int height, boolean focused, boolean hover,
                      Color accent) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int radius = UiTheme.R_MD;

        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, radius, radius));

        g2.setColor(focused ? accent : (hover ? UiTheme.STROKE : UiTheme.STROKE_SOFT));
        g2.setStroke(new java.awt.BasicStroke(focused ? 1.4f : 1f));
        g2.draw(new RoundRectangle2D.Float(0.7f, 0.7f, width - 1.4f, height - 1.4f, radius, radius));

        g2.dispose();
    }

    /** The grey hint inside an empty field. */
    static void paintPlaceholder(Graphics g, int width, int height, String placeholder,
                                 boolean empty) {
        if (!empty || placeholder == null || placeholder.isEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UiTheme.TEXT_MUTE);
        g2.setFont(Fonts.body(13));
        g2.drawString(placeholder, 13, height / 2 + 5);
        g2.dispose();
    }
}
