package com.bubt.findit.ui.common;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/** A small pill that shows a status such as OPEN, APPROVED or SUSPENDED. */
public class StatusBadge extends JComponent {

    private String text = "";
    private Color color = UiTheme.TEXT_DIM;
    private boolean filled = true;

    public StatusBadge(String text, Color color) {
        this.text = text == null ? "" : text;
        this.color = color == null ? UiTheme.TEXT_DIM : color;
        setFont(Fonts.medium(11));
        setOpaque(false);
        setPreferredSize(new Dimension(80, 24));
        setMaximumSize(new Dimension(160, 24));
    }

    public void setStatus(String text, Color color) {
        this.text = text == null ? "" : text;
        this.color = color == null ? UiTheme.TEXT_DIM : color;
        revalidate();
        repaint();
    }

    public void setFilled(boolean value) {
        this.filled = value;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());
        return new Dimension(metrics.stringWidth(text) + 24, 24);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        g2.setColor(filled ? UiTheme.alpha(color, 34) : UiTheme.alpha(color, 18));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        g2.setColor(UiTheme.alpha(color, 150));
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, h, h));

        g2.setColor(color);
        g2.setFont(getFont());
        FontMetrics metrics = g2.getFontMetrics();
        int x = (w - metrics.stringWidth(text)) / 2;
        int y = (h - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(text, x, y);
        g2.dispose();
    }
}
