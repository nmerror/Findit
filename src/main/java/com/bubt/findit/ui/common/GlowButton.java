package com.bubt.findit.ui.common;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A fully custom-painted button: gradient fill for the primary action, a thin
 * luminous outline for secondary actions, plus hover / pressed states and a
 * soft glow.
 */
public class GlowButton extends JButton {

    public enum Kind {
        PRIMARY, GHOST, DANGER, SUBTLE
    }

    private final Kind kind;
    private Color accent = UiTheme.ACCENT;
    private int radius = UiTheme.R_MD;
    private int glowSize = 16;
    private boolean glowOnHover = true;
    private boolean hover;
    private boolean pressed;

    public GlowButton(String text, Kind kind) {
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

    public GlowButton setAccent(Color color) {
        this.accent = color;
        repaint();
        return this;
    }

    public GlowButton setRadius(int value) {
        this.radius = value;
        repaint();
        return this;
    }

    public GlowButton setGlow(int size) {
        this.glowSize = size;
        repaint();
        return this;
    }

    public GlowButton setGlowOnHover(boolean value) {
        this.glowOnHover = value;
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
        int inset = 1;
        RoundRectangle2D shape = new RoundRectangle2D.Float(inset, inset,
                w - inset * 2f, h - inset * 2f, radius, radius);

        boolean active = hover || pressed;

        switch (kind) {
            case PRIMARY -> {
                if (glowOnHover && active) {
                    for (int i = glowSize; i > 0; i--) {
                        float t = 1f - (float) i / glowSize;
                        g2.setColor(UiTheme.alpha(accent, (int) (46 * t)));
                        g2.fill(new RoundRectangle2D.Float(inset - i, inset - i,
                                w - inset * 2f + 2f * i, h - inset * 2f + 2f * i, radius + 2f * i, radius + 2f * i));
                    }
                }
                g2.setPaint(new GradientPaint(0, 0, UiTheme.mix(accent, Color.WHITE, 0.18),
                        0, h, UiTheme.mix(accent, UiTheme.ON_ACCENT, 0.22)));
                g2.fill(shape);
                g2.setColor(UiTheme.alpha(Color.WHITE, pressed ? 40 : 70));
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(shape);
                setForeground(UiTheme.ON_ACCENT);
            }
            case DANGER -> {
                if (glowOnHover && active) {
                    for (int i = glowSize; i > 0; i--) {
                        float t = 1f - (float) i / glowSize;
                        g2.setColor(UiTheme.alpha(UiTheme.ROSE, (int) (40 * t)));
                        g2.fill(new RoundRectangle2D.Float(inset - i, inset - i,
                                w - inset * 2f + 2f * i, h - inset * 2f + 2f * i, radius + 2f * i, radius + 2f * i));
                    }
                }
                g2.setPaint(new GradientPaint(0, 0, UiTheme.mix(UiTheme.ROSE, Color.WHITE, 0.12),
                        0, h, UiTheme.mix(UiTheme.ROSE, UiTheme.ON_ACCENT, 0.18)));
                g2.fill(shape);
                setForeground(UiTheme.ON_ACCENT);
            }
            case GHOST -> {
                g2.setColor(active ? UiTheme.alpha(accent, 28) : UiTheme.alpha(UiTheme.SURFACE_2, 140));
                g2.fill(shape);
                g2.setColor(active ? UiTheme.alpha(accent, 190) : UiTheme.STROKE);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(shape);
                setForeground(active ? UiTheme.mix(accent, Color.WHITE, 0.25) : UiTheme.TEXT_DIM);
            }
            default -> {
                g2.setColor(active ? UiTheme.SURFACE_2 : UiTheme.alpha(UiTheme.SURFACE_2, 90));
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
        return new Dimension(Math.max(size.width + 34, 120), Math.max(size.height + 14, 42));
    }
}
