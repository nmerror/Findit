package com.bubt.findit.ui.common;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows the photo of a post inside a table cell.
 *
 * <p>The picture is scaled down once and kept in a small cache, so scrolling
 * through a long table does not load the same file again and again. When the
 * post has no photo, a grey placeholder is drawn instead.
 */
public class PhotoRenderer extends DefaultTableCellRenderer {

    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();
    private static final int SIZE = 40;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, "", isSelected, hasFocus, row, column);
        label.setIcon(null);
        label.setText("");
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 4));
        label.setHorizontalAlignment(JLabel.CENTER);

        String path = value == null ? null : value.toString();
        if (path != null && !path.isBlank()) {
            label.setIcon(load(path));
        }
        label.setBackground(isSelected ? UiTheme.SELECTION
                : (row % 2 == 0 ? UiTheme.SURFACE : new java.awt.Color(0xFAFBFD)));
        return label;
    }

    /** Loads the picture once and scales it to the size of the cell. */
    private static ImageIcon load(String path) {
        return CACHE.computeIfAbsent(path, key -> {
            File file = new File(key);
            if (!file.exists()) {
                return null;
            }
            ImageIcon original = new ImageIcon(key);
            int width = original.getIconWidth();
            int height = original.getIconHeight();
            if (width <= 0 || height <= 0) {
                return null;
            }
            double factor = Math.min((double) SIZE / width, (double) SIZE / height);
            int scaledWidth = Math.max(1, (int) (width * factor));
            int scaledHeight = Math.max(1, (int) (height * factor));
            return new ImageIcon(original.getImage().getScaledInstance(
                    scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH));
        });
    }
}
