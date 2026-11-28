package com.bubt.findit.ui.common;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** A rounded, dark input field with a luminous focus ring and a placeholder. */
public class ModernTextField extends JTextField {

    private String placeholder = "";
    private Color accent = UiTheme.ACCENT;
    private boolean hover;

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        setOpaque(false);
        setForeground(UiTheme.TEXT);
        setCaretColor(UiTheme.ACCENT);
        setSelectionColor(UiTheme.alpha(UiTheme.ACCENT, 90));
        setSelectedTextColor(UiTheme.TEXT);
        setFont(Fonts.body(13));
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

    public ModernTextField setAccent(Color color) {
        this.accent = color;
        return this;
    }

    public ModernTextField setPlaceholder(String value) {
        this.placeholder = value == null ? "" : value;
        repaint();
        return this;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // The editor of Swing is opaque by default and would paint its own
        // background over the placeholder.
        for (java.awt.Component child : getComponents()) {
            if (child instanceof javax.swing.text.JTextComponent editor) {
                editor.setOpaque(false);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FieldSkin.paint(g2, getWidth(), getHeight(), hasFocus(), hover, accent);
        g2.dispose();

        super.paintComponent(g);

        FieldSkin.paintPlaceholder(g, getWidth(), getHeight(), placeholder,
                getText() == null || getText().isEmpty());
    }
}
