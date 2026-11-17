package com.bubt.findit.ui.common;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

/**
 * Renders a status column as a coloured pill that keeps its natural width
 * instead of stretching across the whole cell.
 */
public class StatusTableRenderer implements TableCellRenderer {

    private final JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
    private final StatusBadge badge = new StatusBadge("", UiTheme.TEXT_DIM);

    public StatusTableRenderer() {
        wrapper.setOpaque(false);
        wrapper.add(badge);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        String status = value == null ? "" : value.toString();
        badge.setStatus(status, UiTheme.accentFor(status));
        badge.setFilled(!isSelected);

        if (isSelected) {
            wrapper.setBackground(UiTheme.SELECTION);
            wrapper.setOpaque(true);
        } else {
            wrapper.setOpaque(false);
            wrapper.setBackground(UiTheme.SURFACE);
        }
        return wrapper;
    }
}
