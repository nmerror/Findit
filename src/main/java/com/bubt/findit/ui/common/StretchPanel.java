package com.bubt.findit.ui.common;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

/**
 * A panel that fills the width of its scroll pane.
 *
 * <p>Without this, a JPanel inside a JScrollPane keeps its own preferred
 * width and the content is either cut off or a horizontal scrollbar appears.
 * With {@code tracksViewportWidth}, the panel always matches the viewport,
 * so cards stretch exactly like they do in a real design tool.
 */
public class StretchPanel extends JPanel implements Scrollable {

    public StretchPanel(LayoutManager layout) {
        super(layout);
    }

    /** A stretchable panel with a vertical box layout. */
    public static StretchPanel vertical() {
        StretchPanel panel = new StretchPanel(null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
