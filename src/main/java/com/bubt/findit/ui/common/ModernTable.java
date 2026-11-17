package com.bubt.findit.ui.common;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Table styling: dark rows, no grid lines, a slim accent-highlighted header
 * and a subtle selection colour.
 */
public final class ModernTable {

    private ModernTable() {
    }

    public static JTable style(JTable table) {
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(UiTheme.SELECTION);
        table.setSelectionForeground(UiTheme.TEXT);
        table.setBackground(UiTheme.SURFACE);
        table.setForeground(UiTheme.TEXT);
        table.setFont(Fonts.body(13));
        table.setBorder(javax.swing.BorderFactory.createEmptyBorder());

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new java.awt.Dimension(10, 40));
        header.setBackground(UiTheme.SURFACE_2);
        header.setForeground(UiTheme.TEXT_MUTE);
        header.setFont(Fonts.medium(11));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        header.setDefaultRenderer(new HeaderRenderer());

        TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);
        table.setDefaultRenderer(Object.class, (tbl, value, isSelected, hasFocus, row, column) -> {
            Component component = defaultRenderer.getTableCellRendererComponent(
                    tbl, value, isSelected, hasFocus, row, column);
            if (component instanceof javax.swing.JLabel label) {
                label.setFont(Fonts.body(13));
                label.setForeground(isSelected ? UiTheme.TEXT : UiTheme.TEXT_DIM);
                label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 14, 0, 10));
                label.setOpaque(true);
                label.setBackground(isSelected ? UiTheme.SELECTION
                        : (row % 2 == 0 ? UiTheme.SURFACE : UiTheme.alpha(UiTheme.SURFACE_2, 120)));
            }
            return component;
        });
        return table;
    }

    /** A scroll pane that fits the dark look. */
    public static JScrollPane scroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UiTheme.SURFACE);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getHorizontalScrollBar().setOpaque(false);
        return scroll;
    }

    /** Header cells: uppercase, dim, with a hairline separator. */
    private static final class HeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setFont(Fonts.medium(11));
            label.setForeground(UiTheme.TEXT_MUTE);
            label.setBackground(UiTheme.SURFACE_2);
            label.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createEmptyBorder(0, 14, 0, 10),
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.STROKE_SOFT)));
            label.setOpaque(true);
            return label;
        }
    }
}
