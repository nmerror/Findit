package com.bubt.findit.ui.common;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A card: a flat surface with a thin border and slightly rounded corners.
 *
 * <p>This is the base of every panel of the interface. It is deliberately
 * plain - one fill colour and one border - so that a screen made of many
 * cards still looks calm.
 */
public class RoundedPanel extends JPanel {

    private int radius = UiTheme.R_LG;
    private Color fill = UiTheme.SURFACE;
    private Color border = UiTheme.STROKE;

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

    /** The fill colour of the card. */
    public RoundedPanel setFillColor(Color value) {
        this.fill = value;
        repaint();
        return this;
    }

    /** The border colour of the card. */
    public RoundedPanel setBorderColor(Color value) {
        this.border = value;
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

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

        if (border != null) {
            g2.setColor(border);
            g2.setStroke(new java.awt.BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, radius, radius));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
