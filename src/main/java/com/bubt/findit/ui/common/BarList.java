package com.bubt.findit.ui.common;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Horizontal bars for the "busiest locations" chart - each bar is a gradient
 * that fades towards its end, which reads as a modern data panel.
 */
public class BarList extends JComponent {

    private record Entry(String label, int value, Color color) {
    }

    private final List<Entry> entries = new ArrayList<>();

    public BarList() {
        setPreferredSize(new Dimension(320, 160));
        setOpaque(false);
    }

    public void setItems(List<com.bubt.findit.core.model.LocationCount> items) {
        entries.clear();
        if (items != null) {
            Color[] palette = {UiTheme.ACCENT, UiTheme.VIOLET, UiTheme.MINT, UiTheme.AMBER, UiTheme.ROSE};
            int index = 0;
            for (com.bubt.findit.core.model.LocationCount item : items) {
                entries.add(new Entry(item.getLabel(), item.getCount(),
                        palette[index % palette.length]));
                index++;
            }
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (entries.isEmpty()) {
            g2.setColor(UiTheme.TEXT_MUTE);
            g2.setFont(Fonts.body(13));
            g2.drawString("No location data yet.", 4, 24);
            g2.dispose();
            return;
        }

        int max = entries.stream().mapToInt(Entry::value).max().orElse(1);
        int rowHeight = 30;
        int barWidth = getWidth() - 150;

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            int y = 6 + i * rowHeight;

            g2.setColor(UiTheme.TEXT_DIM);
            g2.setFont(Fonts.body(12));
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(entry.label(), 0, y + 14);

            int trackX = 108;
            int width = Math.max(6, (int) (barWidth * (entry.value() / (double) max)));
            g2.setColor(UiTheme.alpha(UiTheme.SURFACE_3, 120));
            g2.fill(new RoundRectangle2D.Float(trackX, y + 3, Math.max(barWidth, 10), 13, 7, 7));

            g2.setPaint(new java.awt.GradientPaint(trackX, y, entry.color(),
                    trackX + width, y, UiTheme.alpha(entry.color(), 60)));
            g2.fill(new RoundRectangle2D.Float(trackX, y + 3, width, 13, 7, 7));

            g2.setColor(UiTheme.TEXT);
            g2.setFont(Fonts.medium(12));
            metrics = g2.getFontMetrics();
            g2.drawString(String.valueOf(entry.value()), getWidth() - metrics.stringWidth(
                    String.valueOf(entry.value())) - 2, y + 14);
        }
        g2.dispose();
    }
}
