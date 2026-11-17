package com.bubt.findit.ui.common;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/**
 * A KPI card: small caption, big number, supporting line and a glyph badge.
 * The number is the hero - everything else stays quiet.
 */
public class StatCard extends RoundedPanel {

    private final JLabel caption = new JLabel();
    private final JLabel value = new JLabel("0");
    private final JLabel detail = new JLabel(" ");
    private final GlyphBadge badge;

    public StatCard(String captionText, int value, String detailText, Icons.Glyph glyph, Color accent) {
        super(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        setLayout(new java.awt.BorderLayout(0, 6));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18));

        caption.setText(captionText);
        caption.setFont(Fonts.caption(11));
        caption.setForeground(UiTheme.TEXT_MUTE);

        this.value.setFont(Fonts.display(32));
        this.value.setForeground(UiTheme.TEXT);

        detail.setText(detailText);
        detail.setFont(Fonts.caption(12));
        detail.setForeground(UiTheme.TEXT_DIM);

        badge = new GlyphBadge(glyph, accent);

        JPanel header = new JPanel(new java.awt.BorderLayout());
        header.setOpaque(false);
        header.add(caption, java.awt.BorderLayout.WEST);
        header.add(badge, java.awt.BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        this.value.setAlignmentX(0.5f);
        body.add(this.value);
        body.add(Box.createVerticalStrut(2));
        detail.setAlignmentX(0.5f);
        body.add(detail);

        add(header, java.awt.BorderLayout.NORTH);
        add(body, java.awt.BorderLayout.CENTER);
        setPreferredSize(new Dimension(190, 132));

        setValue(value);
    }

    public void setValue(int newValue) {
        value.setText(String.valueOf(newValue));
    }

    public void setValue(String newValue) {
        value.setText(newValue);
    }

    public void setDetail(String text) {
        detail.setText(text == null || text.isEmpty() ? " " : text);
    }

    public void setAccent(Color accent) {
        value.setForeground(UiTheme.mix(accent, Color.WHITE, 0.25));
    }

    /** A circular badge that holds one vector glyph. */
    public static class GlyphBadge extends JComponent {

        private final Icons.Glyph glyph;
        private final Color accent;

        public GlyphBadge(Icons.Glyph glyph, Color accent) {
            this.glyph = glyph;
            this.accent = accent;
            setPreferredSize(new Dimension(38, 38));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int d = Math.min(getWidth(), getHeight());
            g2.setColor(UiTheme.alpha(accent, 30));
            g2.fill(new Ellipse2D.Float(0, 0, d, d));
            g2.setColor(UiTheme.alpha(accent, 90));
            g2.setStroke(new java.awt.BasicStroke(1f));
            g2.draw(new Ellipse2D.Float(0.5f, 0.5f, d - 1f, d - 1f));

            Icons.draw(g2, glyph, (d - 17) / 2, (d - 17) / 2, 17, accent);
            g2.dispose();
        }
    }
}
