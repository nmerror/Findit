package com.bubt.findit.ui.common;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * The screen shown where another segment's work will go. It is styled like
 * the rest of the application and states exactly what that teammate has to
 * build and where it plugs in.
 */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String segmentName, String screenName, String description) {
        this(segmentName, screenName, description, null);
    }

    public PlaceholderPanel(String segmentName, String screenName, String description,
                            String contractHint) {
        super(new BorderLayout());
        setBackground(UiTheme.WINDOW);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 26, 24, 26));

        RoundedPanel card = new RoundedPanel(UiTheme.R_XL, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(34, 38, 34, 38));

        // --- icon -------------------------------------------------------
        JComponent emblem = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                g2.setColor(UiTheme.alpha(UiTheme.ACCENT, 22));
                g2.fillRoundRect(0, 0, size, size, 20, 20);
                g2.setColor(UiTheme.alpha(UiTheme.ACCENT, 120));
                g2.setStroke(new java.awt.BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, size - 1, size - 1, 20, 20);
                Icons.draw(g2, Icons.Glyph.BOX, (size - 30) / 2, (size - 30) / 2, 30, UiTheme.ACCENT);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(74, 74);
            }
        };

        // --- badge ------------------------------------------------------
        JLabel badge = new JLabel(segmentName);
        badge.setFont(Fonts.medium(11));
        badge.setForeground(UiTheme.ACCENT);
        badge.setOpaque(true);
        badge.setBackground(UiTheme.alpha(UiTheme.ACCENT, 26));
        badge.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UiTheme.alpha(UiTheme.ACCENT, 90)),
                javax.swing.BorderFactory.createEmptyBorder(5, 12, 5, 12)));

        JPanel badgeRow = new JPanel(new BorderLayout());
        badgeRow.setOpaque(false);
        badgeRow.add(emblem, BorderLayout.WEST);
        JPanel badgeWrap = new JPanel();
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);
        badgeRow.add(badgeWrap, BorderLayout.CENTER);

        // --- text -------------------------------------------------------
        JLabel heading = new JLabel(screenName);
        heading.setFont(Fonts.display(26));
        heading.setForeground(UiTheme.TEXT);

        JLabel body = new JLabel("<html>"
                + escape(description).replace("\n", "<br>") + "</html>");
        body.setFont(Fonts.body(13));
        body.setForeground(UiTheme.TEXT_DIM);
        body.setPreferredSize(new Dimension(660, 92));
        body.setMaximumSize(new Dimension(660, 92));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        heading.setAlignmentX(LEFT_ALIGNMENT);
        body.setAlignmentX(LEFT_ALIGNMENT);
        text.add(heading);
        text.add(Box.createVerticalStrut(10));
        text.add(body);

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        header.add(badgeRow, BorderLayout.WEST);
        header.add(text, BorderLayout.CENTER);

        card.add(header, BorderLayout.CENTER);

        if (contractHint != null && !contractHint.isBlank()) {
            JLabel hint = new JLabel(contractHint);
            hint.setFont(Fonts.mono(11));
            hint.setForeground(UiTheme.ACCENT);
            hint.setOpaque(true);
            hint.setBackground(UiTheme.alpha(UiTheme.ACCENT, 18));
            hint.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.STROKE_SOFT),
                    javax.swing.BorderFactory.createEmptyBorder(12, 18, 12, 18)));
            card.add(hint, BorderLayout.SOUTH);
        }

        add(card, BorderLayout.CENTER);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
