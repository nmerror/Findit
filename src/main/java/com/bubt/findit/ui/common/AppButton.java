package com.bubt.findit.ui.common;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * The button of the application.
 *
 * <p>Four kinds, all flat: a filled accent button for the main action, a
 * bordered button for the secondary actions, a red one for the dangerous ones
 * and a quiet one for the rest. There is no glow and no gradient - the hover
 * state is a slightly different fill.
 */
public class AppButton extends JButton {

    public enum Kind {
        PRIMARY, SECONDARY, DANGER, SUBTLE
    }

    private final Kind kind;
    private Color accent = UiTheme.ACCENT;
    private int radius = UiTheme.R_MD;
    private boolean hover;
    private boolean pressed;

    public AppButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Fonts.medium(13));
        setForeground(UiTheme.TEXT);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    /** Changes the accent colour of a primary button. */
    public AppButton setAccent(Color color) {
        this.accent = color;
        repaint();
        return this;
    }

    public AppButton setRadius(int value) {
        this.radius = value;
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
        float inset = 1f;
        RoundRectangle2D shape = new RoundRectangle2D.Float(inset, inset,
                w - inset * 2f, h - inset * 2f, radius, radius);
        boolean active = hover || pressed;

        switch (kind) {
            case PRIMARY -> {
                g2.setColor(active ? UiTheme.alpha(Color.BLACK, pressed ? 40 : 18) : accent);
                g2.fill(shape);
                setForeground(UiTheme.ON_ACCENT);
            }
            case DANGER -> {
                g2.setColor(active ? UiTheme.alpha(UiTheme.ROSE, pressed ? 60 : 30)
                        : UiTheme.alpha(UiTheme.ROSE, 18));
                g2.fill(shape);
                g2.setColor(UiTheme.ROSE);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(shape);
                setForeground(UiTheme.ROSE);
            }
            case SECONDARY -> {
                g2.setColor(active ? UiTheme.SURFACE_2 : UiTheme.SURFACE);
                g2.fill(shape);
                g2.setColor(active ? accent : UiTheme.STROKE);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(shape);
                setForeground(active ? UiTheme.ACCENT_DEEP : UiTheme.TEXT_DIM);
            }
            default -> {
                g2.setColor(active ? UiTheme.SURFACE_3 : UiTheme.SURFACE_2);
                g2.fill(shape);
                setForeground(UiTheme.TEXT_DIM);
            }
        }

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(Math.max(size.width + 26, 110), Math.max(size.height + 12, 38));
    }
}
