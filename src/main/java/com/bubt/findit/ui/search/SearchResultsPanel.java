package com.bubt.findit.ui.search;

import com.bubt.findit.core.model.SearchHit;
import com.bubt.findit.service.SearchService;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.Icons;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.StatusBadge;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * The search results screen (Section 6 - "Search and Report Generation").
 *
 * <p>Segment 1 searches the users table; every other segment can add its own
 * rows by registering a {@code SearchContributor}.
 */
public class SearchResultsPanel extends JPanel {

    private final SearchService searchService;
    private final JLabel heading = new JLabel();
    private final JLabel summary = new JLabel(" ");
    private final JPanel rows = new JPanel();

    private String query = "";

    public SearchResultsPanel(SearchService searchService) {
        super(new BorderLayout(0, 14));
        this.searchService = searchService;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        heading.setFont(Fonts.display(24));
        heading.setForeground(UiTheme.TEXT);

        summary.setFont(Fonts.body(12));
        summary.setForeground(UiTheme.TEXT_MUTE);

        JPanel head = new JPanel(new BorderLayout(0, 3));
        head.setOpaque(false);
        head.add(heading, BorderLayout.NORTH);
        head.add(summary, BorderLayout.SOUTH);

        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        add(head, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        setQuery("");
    }

    /** Runs the search and shows the result. */
    public void setQuery(String value) {
        this.query = value == null ? "" : value.trim();
        heading.setText(query.isEmpty() ? "Search" : "Results for \"" + query + "\"");

        rows.removeAll();
        if (query.isEmpty()) {
            summary.setText("Type in the search box above to look for people, items or claims.");
            rows.add(emptyCard("Start typing to search", Icons.Glyph.SEARCH));
            revalidate();
            repaint();
            return;
        }

        List<SearchHit> hits = searchService.search(query, 50);
        summary.setText(hits.size() + (hits.size() == 1 ? " result" : " results")
                + "  ·  people, items and claims");

        if (hits.isEmpty()) {
            rows.add(emptyCard("Nothing found for \"" + query + "\"", Icons.Glyph.SEARCH));
        } else {
            for (SearchHit hit : hits) {
                rows.add(hitRow(hit));
                rows.add(Box.createVerticalStrut(8));
            }
        }
        revalidate();
        repaint();
    }

    private JComponent hitRow(SearchHit hit) {
        RoundedPanel row = new RoundedPanel(UiTheme.R_MD, UiTheme.SURFACE, UiTheme.STROKE);
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JComponent glyph = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                Color accent = accentFor(hit.getKind());
                g2.setColor(UiTheme.alpha(accent, 28));
                g2.fillRoundRect(0, 0, size, size, 12, 12);
                Icons.draw(g2, glyphFor(hit.getKind()), (size - 18) / 2, (size - 18) / 2, 18, accent);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(38, 38);
            }
        };

        JLabel title = new JLabel(hit.getTitle());
        title.setFont(Fonts.medium(13));
        title.setForeground(UiTheme.TEXT);

        JLabel subtitle = new JLabel(hit.getSubtitle() == null ? "" : hit.getSubtitle());
        subtitle.setFont(Fonts.caption(12));
        subtitle.setForeground(UiTheme.TEXT_MUTE);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        texts.add(title);
        texts.add(subtitle);

        StatusBadge source = new StatusBadge(hit.getSource(), accentFor(hit.getKind()));

        row.add(glyph, BorderLayout.WEST);
        row.add(texts, BorderLayout.CENTER);
        row.add(source, BorderLayout.EAST);
        return row;
    }

    private JComponent emptyCard(String message, Icons.Glyph glyph) {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JComponent emblem = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                g2.setColor(UiTheme.alpha(UiTheme.ACCENT, 20));
                g2.fillRoundRect(0, 0, size, size, 18, 18);
                Icons.draw(g2, glyph, (size - 26) / 2, (size - 26) / 2, 26, UiTheme.ACCENT);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(58, 58);
            }
        };

        JLabel text = new JLabel(message);
        text.setFont(Fonts.body(14));
        text.setForeground(UiTheme.TEXT_DIM);

        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);
        head.add(emblem, BorderLayout.WEST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(head, BorderLayout.NORTH);
        wrap.add(text, BorderLayout.CENTER);

        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private Icons.Glyph glyphFor(String kind) {
        if (kind == null) {
            return Icons.Glyph.BOX;
        }
        return switch (kind.toUpperCase()) {
            case "PERSON" -> Icons.Glyph.USERS;
            case "ITEM" -> Icons.Glyph.BOX;
            case "CLAIM" -> Icons.Glyph.TAG;
            case "MATCH" -> Icons.Glyph.CHART;
            default -> Icons.Glyph.BOX;
        };
    }

    private Color accentFor(String kind) {
        if (kind == null) {
            return UiTheme.ACCENT;
        }
        return switch (kind.toUpperCase()) {
            case "PERSON" -> UiTheme.VIOLET;
            case "ITEM" -> UiTheme.ACCENT;
            case "CLAIM" -> UiTheme.AMBER;
            case "MATCH" -> UiTheme.MINT;
            default -> UiTheme.ACCENT;
        };
    }
}
