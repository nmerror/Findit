package com.bubt.findit.ui.auth;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * The button of the entry screens: a white button with black text for the
 * main action, a grey outline for the secondary ones.
 */
public class AuthButton extends JButton {

    public enum Kind {
        PRIMARY, SECONDARY
    }

    private final Kind kind;
    private boolean hover;

    public AuthButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 13));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        if (kind == Kind.PRIMARY) {
            g2.setColor(hover ? new Color(0xE4E8EC) : Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            setForeground(new Color(0x111418));
        } else {
            g2.setColor(hover ? AuthColors.FIELD : AuthColors.CARD);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            g2.setColor(AuthColors.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
            setForeground(AuthColors.TEXT_DIM);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(Math.max(size.width + 26, 110), Math.max(size.height + 12, 42));
    }
}
