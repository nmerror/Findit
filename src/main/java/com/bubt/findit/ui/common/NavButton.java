package com.bubt.findit.ui.common;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A sidebar navigation item: a vector glyph, a label, a hover tint and a
 * bright accent bar when it is the active screen.
 *
 * <p>The whole item is painted by hand, so the look stays identical on every
 * platform and in every look-and-feel.
 */
public class NavButton extends JButton {

    private final Icons.Glyph glyph;
    private boolean active;
    private boolean hover;

    public NavButton(String text, Icons.Glyph glyph) {
        super(text);
        this.glyph = glyph;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setHorizontalAlignment(LEFT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Fonts.medium(13));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(11, 14, 11, 12));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(220, 44));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                hover = false;
                repaint();
            }
        });
    }

    public void setActiveState(boolean value) {
        this.active = value;
        repaint();
    }

    public boolean isActiveState() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (active) {
            g2.setColor(UiTheme.alpha(UiTheme.ACCENT, 26));
            g2.fillRoundRect(0, 0, w, h, UiTheme.R_SM, UiTheme.R_SM);
            g2.setColor(UiTheme.ACCENT);
            g2.fillRoundRect(0, 5, 3, h - 10, 3, 3);
        } else if (hover) {
            g2.setColor(UiTheme.SURFACE_2);
            g2.fillRoundRect(0, 0, w, h, UiTheme.R_SM, UiTheme.R_SM);
        }

        Color glyphColor = active ? UiTheme.ACCENT : (hover ? UiTheme.TEXT : UiTheme.TEXT_MUTE);
        Color textColor = active ? UiTheme.TEXT : (hover ? UiTheme.TEXT : UiTheme.TEXT_DIM);

        int glyphSize = 17;
        int glyphX = 14;
        int glyphY = (h - glyphSize) / 2;
        Icons.draw(g2, glyph, glyphX, glyphY, glyphSize, glyphColor);

        g2.setColor(textColor);
        g2.setFont(Fonts.medium(13));
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(getText(), glyphX + glyphSize + 12,
                (h + metrics.getAscent() - metrics.getDescent()) / 2);

        g2.dispose();
    }
}
