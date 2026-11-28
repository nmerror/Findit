package com.bubt.findit.ui.common;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * A horizontal bar that shows how a total is spread over several groups.
 *
 * <p>The bar itself is split into one flat segment per group, and the legend
 * below it prints the name and the number of every group. It replaces the
 * pie chart of the first version - a number is easier to read than an angle.
 */
public class StackedBar extends JComponent {

    /** One group of the bar. */
    public record Slice(String label, int value, Color color) {
    }

    private final List<Slice> slices = new ArrayList<>();
    private String totalLabel = "in total";

    public StackedBar() {
        setPreferredSize(new Dimension(320, 96));
        setOpaque(false);
    }

    /** Replaces the groups of the bar. */
    public void setSlices(List<Slice> newSlices) {
        slices.clear();
        if (newSlices != null) {
            slices.addAll(newSlices);
        }
        revalidate();
        repaint();
    }

    /** The text behind the total, for example "in total". */
    public void setTotalLabel(String label) {
        this.totalLabel = label == null ? "" : label;
        repaint();
    }

    /** The sum of all groups. */
    public int total() {
        return slices.stream().mapToInt(Slice::value).sum();
    }

    /** "1 claim" instead of "1 claims". */
    private String singular(String label) {
        if (label.endsWith("s")) {
            return label.substring(0, label.length() - 1);
        }
        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int barHeight = 18;
        int total = total();

        g2.setColor(UiTheme.SURFACE_3);
        g2.fillRoundRect(0, 0, w, barHeight, 9, 9);

        if (total > 0) {
            int x = 0;
            boolean last = false;
            for (int i = 0; i < slices.size(); i++) {
                Slice slice = slices.get(i);
                if (slice.value() <= 0) {
                    continue;
                }
                last = i == slices.size() - 1;
                int width = Math.max(4, (int) Math.round(w * (slice.value() / (double) total)));
                if (last) {
                    width = w - x;
                }
                g2.setColor(slice.color());
                g2.fillRect(x, 0, Math.max(0, width - (last ? 0 : 3)), barHeight);
                x += width;
            }
        }

        int legendTop = barHeight + 16;
        int columnWidth = Math.max(120, w / 2);
        int rowHeight = 22;

        for (int i = 0; i < slices.size(); i++) {
            Slice slice = slices.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = column * columnWidth;
            int y = legendTop + row * rowHeight;

            g2.setColor(slice.color());
            g2.fillRoundRect(x, y + 5, 10, 10, 3, 3);

            g2.setColor(UiTheme.TEXT_DIM);
            g2.setFont(Fonts.body(12));
            g2.drawString(slice.label(), x + 18, y + 15);

            String value = String.valueOf(slice.value());
            FontMetrics metrics = g2.getFontMetrics();
            g2.setColor(UiTheme.TEXT);
            g2.setFont(Fonts.medium(12));
            g2.drawString(value, x + 18 + metrics.stringWidth(slice.label()) + 10, y + 15);
        }

        if (total > 0) {
            g2.setColor(UiTheme.TEXT_MUTE);
            g2.setFont(Fonts.caption(11));
            g2.drawString(total + " " + (total == 1 ? singular(totalLabel) : totalLabel),
                    0, legendTop + 2 * rowHeight + 6);
        }
        g2.dispose();
    }
}
