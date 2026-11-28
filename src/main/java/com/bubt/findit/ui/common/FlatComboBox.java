package com.bubt.findit.ui.common;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Function;

/**
 * A drop-down that looks like the rest of the application.
 *
 * <p>The standard Swing combo box is drawn by the look and feel, so it looks
 * different from the flat cards around it. This class paints its own white
 * surface with a soft border, draws the selected text itself and replaces the
 * arrow with a small chevron.
 *
 * @param <T> the type of the items
 */
public class FlatComboBox<T> extends JComboBox<T> {

    /** The height of the drop-down - the same as the other input fields. */
    public static final int HEIGHT = 42;

    private static final int TEXT_LEFT = 12;

    private Function<T, String> itemLabels;

    public FlatComboBox(T[] items) {
        super(items);
        style();
    }

    /** Applies the flat look to the drop-down. */
    public final void style() {
        setOpaque(false);
        setBackground(UiTheme.SURFACE);
        setForeground(UiTheme.TEXT);
        setFont(Fonts.body(13));
        setBorder(BorderFactory.createEmptyBorder(0, TEXT_LEFT, 0, 34));
        setFocusable(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        setUI(new FlatComboBoxUI());
    }

    /** Keeps our own look even when the look and feel changes. */
    @Override
    public void updateUI() {
        super.updateUI();
        setUI(new FlatComboBoxUI());
    }

    /**
     * Shows a friendly text for every item instead of {@code toString()}.
     *
     * @param labels the text of every item
     */
    public void setItemLabels(Function<T, String> labels) {
        this.itemLabels = labels;
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    @SuppressWarnings("unchecked")
                    T item = (T) value;
                    setText(textOf(item));
                }
                setFont(Fonts.body(13));
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 8));
                setOpaque(true);
                setBackground(isSelected ? UiTheme.SELECTION : UiTheme.SURFACE);
                setForeground(UiTheme.TEXT);
                return this;
            }
        });
    }

    /** The text of one item. */
    private String textOf(T item) {
        if (itemLabels != null) {
            return itemLabels.apply(item);
        }
        return String.valueOf(item);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UiTheme.SURFACE);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), UiTheme.R_LG, UiTheme.R_LG));
        g2.setColor(UiTheme.STROKE);
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1,
                UiTheme.R_LG, UiTheme.R_LG));
        g2.dispose();

        super.paintComponent(g);

        // The selected text is drawn here - the look and feel only draws the
        // arrow, so the box looks the same as the other input fields.
        Object selected = getSelectedItem();
        if (selected == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        T item = (T) selected;
        String text = textOf(item);
        if (text == null || text.isEmpty()) {
            return;
        }
        Graphics2D g3 = (Graphics2D) g.create();
        g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g3.setFont(getFont());
        g3.setColor(getForeground());
        FontMetrics metrics = g3.getFontMetrics();
        int y = (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2;
        g3.drawString(text, TEXT_LEFT, y);
        g3.dispose();
    }

    /** The look of the drop-down - a flat button with a small chevron. */
    private static final class FlatComboBoxUI extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            return new FlatArrowButton();
        }

        @Override
        protected javax.swing.plaf.basic.ComboPopup createPopup() {
            javax.swing.plaf.basic.ComboPopup popup = super.createPopup();
            if (popup instanceof javax.swing.JPopupMenu) {
                // The open list uses the same soft border as the rest.
                ((javax.swing.JPopupMenu) popup)
                        .setBorder(BorderFactory.createLineBorder(UiTheme.STROKE));
            }
            return popup;
        }

        @Override
        public void update(Graphics g, JComponent c) {
            if (g == null) {
                // The layout is still needed.
                super.update(g, c);
            }
            // Nothing else - the combo box paints its own surface and text.
        }
    }

    /** The chevron of the drop-down - a small grey "v". */
    private static final class FlatArrowButton extends JButton {

        FlatArrowButton() {
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            Dimension size = new Dimension(30, HEIGHT);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiTheme.TEXT_MUTE);
            int size = 6;
            int x = getWidth() - 16;
            int y = (getHeight() - size) / 2;
            Path2D.Double chevron = new Path2D.Double();
            chevron.moveTo(x, y);
            chevron.lineTo(x + size, y);
            chevron.lineTo(x + size / 2.0, y + size);
            chevron.closePath();
            g2.fill(chevron);
            g2.dispose();
        }
    }
}
