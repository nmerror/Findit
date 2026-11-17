package com.bubt.findit.ui.common;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

/**
 * A donut chart used for the claim pipeline (open / approved / returned).
 * Everything is drawn with Graphics2D, so it stays sharp and needs no
 * charting library.
 */
public class DonutChart extends JComponent {

    /** One segment of the ring. */
    public static class Slice {

        private final String label;
        private final int value;
        private final Color color;

        public Slice(String label, int value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public int getValue() {
            return value;
        }

        public Color getColor() {
            return color;
        }
    }

    private java.util.List<Slice> slices = new java.util.ArrayList<>();
    private String centerLabel = "0";
    private String centerCaption = "TOTAL";

    public DonutChart() {
        setPreferredSize(new Dimension(200, 200));
        setOpaque(false);
    }

    public void setSlices(java.util.List<Slice> value) {
        this.slices = value == null ? new java.util.ArrayList<>() : value;
        repaint();
    }

    public void setCenter(String label, String caption) {
        this.centerLabel = label;
        this.centerCaption = caption;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight());
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        double thickness = size * 0.16;

        int total = slices.stream().mapToInt(Slice::getValue).sum();

        // track
        g2.setColor(UiTheme.alpha(UiTheme.SURFACE_3, 120));
        g2.setStroke(new java.awt.BasicStroke((float) thickness, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Float(x, y, size, size));

        if (total > 0) {
            double start = 90;
            for (Slice slice : slices) {
                if (slice.getValue() <= 0) {
                    continue;
                }
                double extent = -360.0 * slice.getValue() / total;
                g2.setColor(slice.getColor());
                g2.setStroke(new java.awt.BasicStroke((float) thickness, java.awt.BasicStroke.CAP_BUTT,
                        java.awt.BasicStroke.JOIN_ROUND));
                g2.draw(new Arc2D.Float(x, y, size, size, (float) start, (float) extent, Arc2D.OPEN));
                start += extent;
            }
        }

        // center text
        g2.setColor(UiTheme.TEXT);
        g2.setFont(Fonts.display(Math.max(18, (int) (size * 0.16))));
        FontMetrics metrics = g2.getFontMetrics();
        int textX = (getWidth() - metrics.stringWidth(centerLabel)) / 2;
        int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent() - 2;
        g2.drawString(centerLabel, textX, textY);

        g2.setColor(UiTheme.TEXT_MUTE);
        g2.setFont(Fonts.medium(10));
        metrics = g2.getFontMetrics();
        int capX = (getWidth() - metrics.stringWidth(centerCaption)) / 2;
        g2.drawString(centerCaption, capX, textY + 16);

        g2.dispose();
    }
}
