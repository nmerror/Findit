package com.bubt.findit.ui.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared painting logic for the futuristic input fields: rounded surface,
 * luminous focus ring, and a placeholder that disappears while typing.
 */
final class FieldSkin {

    private FieldSkin() {
    }

    static void paint(Graphics g, int width, int height, boolean focused, boolean hover,
                      String placeholder, Color accent, boolean empty) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int radius = UiTheme.R_MD;
        float inset = focused ? 1f : 0f;

        g2.setColor(focused ? UiTheme.alpha(accent, 22) : UiTheme.SURFACE_2);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, radius, radius));

        g2.setColor(focused ? accent : (hover ? UiTheme.STROKE : UiTheme.STROKE_SOFT));
        g2.setStroke(new java.awt.BasicStroke(focused ? 1.6f : 1f));
        g2.draw(new RoundRectangle2D.Float(inset, inset, width - inset * 2f, height - inset * 2f,
                radius, radius));

        if (empty && placeholder != null && !placeholder.isEmpty()) {
            g2.setColor(UiTheme.TEXT_MUTE);
            g2.setFont(Fonts.body(13));
            g2.drawString(placeholder, 14, height / 2 + 5);
        }
        g2.dispose();
    }
}
