package com.bubt.findit.ui.common;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/**
 * A deep gradient backdrop with soft glowing orbs - the "deep space" feel
 * behind the login screen and the window background.
 */
public class GradientBackdrop extends JComponent {

    private final Color top;
    private final Color bottom;
    private final Color orbColor;

    public GradientBackdrop(Color top, Color bottom, Color orbColor) {
        this.top = top;
        this.bottom = bottom;
        this.orbColor = orbColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setPaint(new java.awt.GradientPaint(0, 0, top, w, h, bottom));
        g2.fillRect(0, 0, w, h);

        // soft orbs
        paintOrb(g2, w * 0.22f, h * 0.30f, Math.min(w, h) * 0.42f, 34);
        paintOrb(g2, w * 0.78f, h * 0.72f, Math.min(w, h) * 0.34f, 26);
        paintOrb(g2, w * 0.62f, h * 0.18f, Math.min(w, h) * 0.20f, 18);

        // fine grid
        g2.setColor(UiTheme.alpha(UiTheme.GRID, 8));
        g2.setStroke(new java.awt.BasicStroke(1f));
        int step = 46;
        for (int x = 0; x < w; x += step) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += step) {
            g2.drawLine(0, y, w, y);
        }

        g2.dispose();
    }

    private void paintOrb(Graphics2D g2, float cx, float cy, float radius, int alpha) {
        for (int i = 10; i > 0; i--) {
            float t = 1f - (float) i / 10f;
            float r = radius * (0.35f + 0.65f * (1f - t));
            g2.setColor(UiTheme.alpha(orbColor, (int) (alpha * t * 0.5f)));
            g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        }
    }
}
