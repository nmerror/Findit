package com.bubt.findit.ui.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Vector glyphs drawn with Graphics2D - no image files, so the interface
 * stays crisp at any scale and the whole project stays text-only.
 */
public final class Icons {

    private Icons() {
    }

    public enum Glyph {
        DASHBOARD, BOX, SEARCH, SHIELD, USERS, LIST, CHART, TAG, BELL, LOGOUT, LOCK, PLUS, CHECK, KEY,
        SUN, MOON
    }

    /** Draws the glyph inside a {@code size} box whose top-left corner is (x, y). */
    public static void draw(Graphics2D g2, Glyph glyph, int x, int y, int size, Color color) {
        float s = size / 24f;
        g2.setColor(color);
        g2.setStroke(new java.awt.BasicStroke(1.7f * s, java.awt.BasicStroke.CAP_ROUND,
                java.awt.BasicStroke.JOIN_ROUND));

        switch (glyph) {
            case DASHBOARD -> {
                g2.draw(new RoundRectangle2D.Float(x + 2 * s, y + 2 * s, 8 * s, 8 * s, 2 * s, 2 * s));
                g2.draw(new RoundRectangle2D.Float(x + 14 * s, y + 2 * s, 8 * s, 8 * s, 2 * s, 2 * s));
                g2.draw(new RoundRectangle2D.Float(x + 2 * s, y + 14 * s, 8 * s, 8 * s, 2 * s, 2 * s));
                g2.draw(new RoundRectangle2D.Float(x + 14 * s, y + 14 * s, 8 * s, 8 * s, 2 * s, 2 * s));
            }
            case BOX -> {
                g2.draw(new RoundRectangle2D.Float(x + 3 * s, y + 7 * s, 18 * s, 13 * s, 2.5f * s, 2.5f * s));
                java.awt.geom.Path2D path = new java.awt.geom.Path2D.Float();
                path.moveTo(x + 3 * s, y + 10 * s);
                path.lineTo(x + 8 * s, y + 5 * s);
                path.lineTo(x + 16 * s, y + 5 * s);
                path.lineTo(x + 21 * s, y + 10 * s);
                g2.draw(path);
            }
            case SEARCH -> {
                g2.draw(new Ellipse2D.Float(x + 3 * s, y + 3 * s, 13 * s, 13 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 14.5f * s, y + 14.5f * s,
                        x + 21 * s, y + 21 * s));
            }
            case SHIELD -> {
                java.awt.geom.Path2D path = new java.awt.geom.Path2D.Float();
                path.moveTo(x + 12 * s, y + 2 * s);
                path.lineTo(x + 21 * s, y + 6 * s);
                path.lineTo(x + 21 * s, y + 12 * s);
                path.curveTo(x + 21 * s, y + 18 * s, x + 16 * s, y + 21 * s, x + 12 * s, y + 22 * s);
                path.curveTo(x + 8 * s, y + 21 * s, x + 3 * s, y + 18 * s, x + 3 * s, y + 12 * s);
                path.lineTo(x + 3 * s, y + 6 * s);
                path.closePath();
                g2.draw(path);
            }
            case USERS -> {
                g2.draw(new Ellipse2D.Float(x + 4 * s, y + 4 * s, 7 * s, 7 * s));
                g2.draw(new java.awt.geom.Arc2D.Float(x + 1 * s, y + 12 * s, 13 * s, 10 * s,
                        0, 180, java.awt.geom.Arc2D.CHORD));
                g2.draw(new Ellipse2D.Float(x + 14 * s, y + 5 * s, 6 * s, 6 * s));
                g2.draw(new java.awt.geom.Arc2D.Float(x + 12 * s, y + 12 * s, 11 * s, 9 * s,
                        200, 140, java.awt.geom.Arc2D.CHORD));
            }
            case LIST -> {
                for (int i = 0; i < 3; i++) {
                    float cy = (float) (y + (6 + i * 6) * s);
                    g2.draw(new java.awt.geom.Line2D.Float(x + 4 * s, cy, x + 9 * s, cy));
                    g2.draw(new java.awt.geom.Line2D.Float(x + 12 * s, cy, x + 20 * s, cy));
                }
            }
            case CHART -> {
                g2.draw(new java.awt.geom.Line2D.Float(x + 3 * s, y + 3 * s, x + 3 * s, y + 21 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 3 * s, y + 21 * s, x + 21 * s, y + 21 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 6 * s, y + 16 * s, x + 11 * s, y + 10 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 11 * s, y + 10 * s, x + 15 * s, y + 13 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 15 * s, y + 13 * s, x + 20 * s, y + 6 * s));
            }
            case TAG -> {
                java.awt.geom.Path2D path = new java.awt.geom.Path2D.Float();
                path.moveTo(x + 3 * s, y + 12 * s);
                path.lineTo(x + 12 * s, y + 3 * s);
                path.lineTo(x + 21 * s, y + 3 * s);
                path.lineTo(x + 21 * s, y + 12 * s);
                path.lineTo(x + 12 * s, y + 21 * s);
                path.closePath();
                g2.draw(path);
                g2.draw(new Ellipse2D.Float(x + 15 * s, y + 6 * s, 3.2f * s, 3.2f * s));
            }
            case BELL -> {
                java.awt.geom.Path2D path = new java.awt.geom.Path2D.Float();
                path.moveTo(x + 6 * s, y + 17 * s);
                path.curveTo(x + 6 * s, y + 6 * s, x + 8 * s, y + 4 * s, x + 12 * s, y + 4 * s);
                path.curveTo(x + 16 * s, y + 4 * s, x + 18 * s, y + 6 * s, x + 18 * s, y + 17 * s);
                path.closePath();
                g2.draw(path);
                g2.draw(new java.awt.geom.Arc2D.Float(x + 9 * s, y + 17 * s, 6 * s, 5 * s,
                        0, 180, java.awt.geom.Arc2D.OPEN));
            }
            case LOGOUT -> {
                g2.draw(new java.awt.geom.Line2D.Float(x + 13 * s, y + 4 * s, x + 13 * s, y + 20 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 13 * s, y + 12 * s, x + 20 * s, y + 12 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 17 * s, y + 8 * s, x + 21 * s, y + 12 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 21 * s, y + 12 * s, x + 17 * s, y + 16 * s));
                java.awt.geom.Path2D door = new java.awt.geom.Path2D.Float();
                door.moveTo(x + 4 * s, y + 4 * s);
                door.lineTo(x + 10 * s, y + 4 * s);
                door.lineTo(x + 10 * s, y + 20 * s);
                door.lineTo(x + 4 * s, y + 20 * s);
                door.closePath();
                g2.draw(door);
            }
            case LOCK -> {
                g2.draw(new RoundRectangle2D.Float(x + 4 * s, y + 10 * s, 16 * s, 11 * s, 2 * s, 2 * s));
                g2.draw(new java.awt.geom.Arc2D.Float(x + 7.5f * s, y + 3 * s, 9 * s, 12 * s,
                        0, 180, java.awt.geom.Arc2D.OPEN));
            }
            case PLUS -> {
                g2.draw(new java.awt.geom.Line2D.Float(x + 12 * s, y + 5 * s, x + 12 * s, y + 19 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 5 * s, y + 12 * s, x + 19 * s, y + 12 * s));
            }
            case CHECK -> {
                g2.draw(new java.awt.geom.Line2D.Float(x + 5 * s, y + 12 * s, x + 10 * s, y + 17 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 10 * s, y + 17 * s, x + 19 * s, y + 7 * s));
            }
            case SUN -> {
                g2.draw(new Ellipse2D.Float(x + 7 * s, y + 7 * s, 10 * s, 10 * s));
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * i / 4;
                    float x1 = (float) (12 + Math.cos(angle) * 7.5) * (float) s;
                    float y1 = (float) (12 + Math.sin(angle) * 7.5) * (float) s;
                    float x2 = (float) (12 + Math.cos(angle) * 10) * (float) s;
                    float y2 = (float) (12 + Math.sin(angle) * 10) * (float) s;
                    g2.draw(new java.awt.geom.Line2D.Float(x + x1, y + y1, x + x2, y + y2));
                }
            }
            case MOON -> {
                java.awt.geom.Path2D path = new java.awt.geom.Path2D.Float();
                path.moveTo(x + 15 * s, y + 4 * s);
                path.curveTo(x + 7 * s, y + 6 * s, x + 6 * s, y + 17 * s, x + 13 * s, y + 20 * s);
                path.curveTo(x + 17 * s, y + 21 * s, x + 20 * s, y + 19 * s, x + 21 * s, y + 16 * s);
                path.curveTo(x + 16 * s, y + 18 * s, x + 13 * s, y + 12 * s, x + 15 * s, y + 4 * s);
                path.closePath();
                g2.draw(path);
            }
            case KEY -> {
                g2.draw(new Ellipse2D.Float(x + 3 * s, y + 8 * s, 9 * s, 9 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 11 * s, y + 13 * s, x + 21 * s, y + 3 * s));
                g2.draw(new java.awt.geom.Line2D.Float(x + 16 * s, y + 8 * s, x + 19 * s, y + 11 * s));
            }
        }
    }

    /** Convenience overload for a graphics context that is not yet a Graphics2D. */
    public static void draw(Graphics g, Glyph glyph, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw(g2, glyph, x, y, size, color);
        g2.dispose();
    }
}
