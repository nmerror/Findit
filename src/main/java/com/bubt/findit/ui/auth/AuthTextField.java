package com.bubt.findit.ui.auth;

import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * A dark input field with a placeholder - the dark twin of
 * {@code ui.common.ModernTextField}.
 *
 * <p>It is used only by the entry screens; the rest of the application uses
 * the light fields of the design system.
 */
public class AuthTextField extends JTextField {

    private String placeholder = "";
    private boolean focused;
    private boolean hover;

    public AuthTextField(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        setOpaque(true);
        setBackground(AuthColors.CARD);
        setForeground(AuthColors.TEXT);
        setCaretColor(AuthColors.ACCENT);
        setSelectionColor(new Color(0x3E5A66));
        setSelectedTextColor(AuthColors.TEXT);
        setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 13));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
        setPreferredSize(new Dimension(320, 44));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent event) {
                focused = false;
                repaint();
            }
        });
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                repaint();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Swing paints the surface and the text first.
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(focused ? AuthColors.ACCENT : AuthColors.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

        if (getText().isEmpty() && !placeholder.isEmpty()) {
            g2.setColor(AuthColors.TEXT_MUTE);
            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(placeholder, 13, (getHeight() + metrics.getAscent()) / 2 - 2);
        }
        g2.dispose();
    }
}
