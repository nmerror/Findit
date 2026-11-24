package com.bubt.findit.ui.common;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/** A circular avatar showing the user's initials. */
public class AvatarLabel extends JComponent {

    private String initials = "?";
    private Color accent = UiTheme.ACCENT;
    private int size = 38;

    public AvatarLabel(String initials, Color accent, int size) {
        this.initials = initials == null || initials.isBlank() ? "?" : initials;
        this.accent = accent == null ? UiTheme.ACCENT : accent;
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int d = Math.min(getWidth(), getHeight());
        g2.setPaint(new java.awt.GradientPaint(0, 0, UiTheme.mix(accent, Color.WHITE, 0.25),
                d, d, UiTheme.mix(accent, UiTheme.ON_ACCENT, 0.30)));
        g2.fill(new Ellipse2D.Float(0, 0, d, d));

        g2.setColor(UiTheme.alpha(Color.WHITE, 60));
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.draw(new Ellipse2D.Float(0.5f, 0.5f, d - 1f, d - 1f));

        g2.setColor(UiTheme.ON_ACCENT);
        g2.setFont(Fonts.medium((int) Math.max(11, d / 2.6f)));
        FontMetrics metrics = g2.getFontMetrics();
        int x = (d - metrics.stringWidth(initials)) / 2;
        int y = (d - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(initials, x, y);
        g2.dispose();
    }
}
