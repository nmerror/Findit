package com.bubt.findit.ui.common;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A panel with rounded corners, an optional luminous border and an optional
 * gradient fill. This is the base of every card in the new interface.
 */
public class RoundedPanel extends JPanel {

    private int radius = UiTheme.R_LG;
    private Color fill = UiTheme.SURFACE;
    private Color fillTop;
    private Color border = UiTheme.STROKE;
    private Color glow;
    private int glowSize = 18;
    private boolean drawBorder = true;

    public RoundedPanel() {
        this(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
    }

    public RoundedPanel(int radius, Color fill, Color border) {
        this.radius = radius;
        this.fill = fill;
        this.border = border;
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout) {
        this();
        setLayout(layout);
    }

    public RoundedPanel setRadius(int value) {
        this.radius = value;
        repaint();
        return this;
    }

    /** Gradient from {@code top} to {@code fill}. */
    public RoundedPanel setGradient(Color top) {
        this.fillTop = top;
        repaint();
        return this;
    }

    /** Adds a soft coloured halo behind the card. */
    public RoundedPanel setGlow(Color color, int size) {
        this.glow = color;
        this.glowSize = size;
        repaint();
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            g2.dispose();
            super.paintComponent(g);
            return;
        }

        if (glow != null) {
            for (int i = glowSize; i > 0; i--) {
                float t = 1f - (float) i / glowSize;
                g2.setColor(UiTheme.alpha(glow, (int) (16 * t)));
                g2.fill(new RoundRectangle2D.Float(-i, -i, w + 2f * i, h + 2f * i,
                        radius + 2f * i, radius + 2f * i));
            }
        }

        if (fillTop != null) {
            g2.setPaint(new java.awt.GradientPaint(0, 0, fillTop, 0, h, fill));
        } else {
            g2.setPaint(fill);
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

        if (drawBorder && border != null) {
            g2.setColor(border);
            g2.setStroke(new java.awt.BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, radius, radius));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
