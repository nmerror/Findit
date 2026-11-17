package com.bubt.findit.ui.common;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The dark / light switch in the window title bar: a sun in light mode, a
 * moon in dark mode.
 */
public class ThemeToggle extends JComponent {

    private final Runnable onToggle;

    private boolean hover;

    public ThemeToggle(Runnable onToggle) {
        this.onToggle = onToggle;
        setPreferredSize(new Dimension(30, 28));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Switch between dark and light mode");
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hover = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                UiTheme.toggleMode();
                repaint();
                if (onToggle != null) {
                    onToggle.run();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (hover) {
            g2.setColor(UiTheme.alpha(UiTheme.ACCENT, 50));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        }

        Icons.Glyph glyph = UiTheme.isDark() ? Icons.Glyph.MOON : Icons.Glyph.SUN;
        Color color = UiTheme.isDark() ? UiTheme.ACCENT : UiTheme.AMBER;
        Icons.draw(g2, glyph, 6, 5, 18, color);
        g2.dispose();
    }
}
