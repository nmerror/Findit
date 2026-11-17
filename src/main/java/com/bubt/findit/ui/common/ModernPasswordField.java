package com.bubt.findit.ui.common;

import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** The password twin of {@link ModernTextField}. */
public class ModernPasswordField extends JPasswordField {

    private String placeholder = "";
    private Color accent = UiTheme.ACCENT;
    private boolean hover;

    public ModernPasswordField(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        setOpaque(false);
        setForeground(UiTheme.TEXT);
        setCaretColor(UiTheme.ACCENT);
        setSelectionColor(UiTheme.alpha(UiTheme.ACCENT, 90));
        setSelectedTextColor(UiTheme.WINDOW_DEEP);
        setFont(Fonts.body(13));
        setEchoChar('•');
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
        setPreferredSize(new java.awt.Dimension(280, 44));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent event) {
                repaint();
            }
        });
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
        });
    }

    public ModernPasswordField setAccent(Color color) {
        this.accent = color;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FieldSkin.paint(g2, getWidth(), getHeight(), hasFocus(), hover, placeholder, accent,
                getPassword().length == 0);
        g2.dispose();
        super.paintComponent(g);
    }
}
