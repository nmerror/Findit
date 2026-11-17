package com.bubt.findit.ui.dashboard;

import com.bubt.findit.core.model.DashboardStats;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.User;
import com.bubt.findit.service.DashboardService;
import com.bubt.findit.ui.common.BarList;
import com.bubt.findit.ui.common.DonutChart;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.Icons;
import com.bubt.findit.ui.common.ModernTable;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.StatCard;
import com.bubt.findit.ui.common.StretchPanel;
import com.bubt.findit.ui.common.StatusTableRenderer;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * The dashboard of Segment 1.
 *
 * <p>It never queries the tables of the other segments - every number comes
 * from {@link DashboardService}, which asks them through their published
 * contracts.
 */
public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService;
    private final User viewer;

    private final StatCard lostCard;
    private final StatCard foundCard;
    private final StatCard claimsCard;
    private final StatCard usersCard;
    private final DonutChart donut = new DonutChart();
    private final BarList barList = new BarList();
    private final JLabel hintLabel = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Item", "Type", "Category", "Location", "Reported by", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table;

    public DashboardPanel(DashboardService dashboardService, User viewer) {
        super(new BorderLayout());
        this.dashboardService = dashboardService;
        this.viewer = viewer;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        lostCard = new StatCard("Lost items", 0, "Reported by students", Icons.Glyph.BOX, UiTheme.ACCENT);
        foundCard = new StatCard("Found items", 0, "Waiting to be claimed", Icons.Glyph.CHECK, UiTheme.MINT);
        claimsCard = new StatCard("Open claims", 0, "Needs verification", Icons.Glyph.TAG, UiTheme.AMBER);
        usersCard = new StatCard("Users", 0, "Registered accounts", Icons.Glyph.USERS, UiTheme.VIOLET);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(lostCard);
        cards.add(foundCard);
        cards.add(claimsCard);
        cards.add(usersCard);

        table = ModernTable.style(new JTable(model));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {190, 100, 110, 150, 140, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusTableRenderer());

        StretchPanel body = StretchPanel.vertical();
        body.setOpaque(false);

        cards.setAlignmentX(LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        cards.setMinimumSize(new Dimension(560, 140));
        body.add(cards);
        body.add(Box.createVerticalStrut(14));

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0));
        charts.setOpaque(false);
        charts.add(buildDonutCard());
        charts.add(buildLocationsCard());
        charts.setAlignmentX(LEFT_ALIGNMENT);
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        body.add(charts);
        body.add(Box.createVerticalStrut(14));

        body.add(buildItemsCard());

        hintLabel.setFont(Fonts.body(12));
        hintLabel.setForeground(UiTheme.AMBER);
        hintLabel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(Box.createVerticalStrut(10));
        body.add(hintLabel);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    // ------------------------------------------------------------- panels

    private JPanel buildDonutCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        card.add(sectionTitle("Claim pipeline"), BorderLayout.NORTH);

        JPanel chartArea = new JPanel(new BorderLayout());
        chartArea.setOpaque(false);
        donut.setPreferredSize(new Dimension(170, 170));
        chartArea.add(donut, BorderLayout.CENTER);
        chartArea.add(buildLegend(), BorderLayout.EAST);

        card.add(chartArea, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildLegend() {
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.add(legendRow(UiTheme.AMBER, "Open"));
        legend.add(Box.createVerticalStrut(8));
        legend.add(legendRow(UiTheme.MINT, "Approved"));
        legend.add(Box.createVerticalStrut(8));
        legend.add(legendRow(UiTheme.ACCENT, "Returned"));
        legend.add(Box.createVerticalStrut(8));
        legend.add(legendRow(UiTheme.VIOLET, "Pending"));
        return legend;
    }

    private JComponent legendRow(Color color, String text) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(120, 20));

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 3, 10, 10);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(10, 16);
            }
        };
        dot.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setFont(Fonts.body(12));
        label.setForeground(UiTheme.TEXT_DIM);

        row.add(dot, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildLocationsCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        card.add(sectionTitle("Busiest locations"), BorderLayout.NORTH);
        barList.setPreferredSize(new Dimension(320, 170));
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(barList, BorderLayout.CENTER);
        card.add(holder, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildItemsCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(sectionTitle("Recently reported items"), BorderLayout.NORTH);

        JScrollPane scroll = ModernTable.scroll(table);
        scroll.setPreferredSize(new Dimension(600, 230));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(Fonts.medium(11));
        label.setForeground(UiTheme.TEXT_MUTE);
        return label;
    }

    // ------------------------------------------------------------ refresh

    public void refresh() {
        DashboardStats stats = dashboardService.build(viewer);

        lostCard.setValue(stats.getTotalLostItems());
        lostCard.setDetail(stats.getTotalLostItems() + " reports from students");
        foundCard.setValue(stats.getTotalFoundItems());
        foundCard.setDetail(stats.getTotalFoundItems() + " waiting to be claimed");
        claimsCard.setValue(stats.getOpenClaims());
        claimsCard.setDetail(stats.getPendingVerification() + " pending verification");

        if (viewer.isAdmin()) {
            usersCard.setValue(stats.getTotalUsers());
            usersCard.setDetail(stats.getActiveUsers() + " active · " + stats.getSuspendedUsers() + " suspended");
        } else {
            usersCard.setValue(stats.getMyLostReports() + stats.getMyFoundReports());
            usersCard.setDetail(stats.getMyLostReports() + " lost · " + stats.getMyFoundReports() + " found by me");
        }

        int total = stats.getOpenClaims() + stats.getApprovedClaims() + stats.getReturnedItems();
        donut.setSlices(List.of(
                new DonutChart.Slice("Open", stats.getOpenClaims(), UiTheme.AMBER),
                new DonutChart.Slice("Approved", stats.getApprovedClaims(), UiTheme.MINT),
                new DonutChart.Slice("Returned", stats.getReturnedItems(), UiTheme.ACCENT),
                new DonutChart.Slice("Pending", stats.getPendingVerification(), UiTheme.VIOLET)));
        donut.setCenter(String.valueOf(total), "CLAIMS");

        barList.setItems(stats.getTopLocations());

        model.setRowCount(0);
        List<ItemSummary> items = stats.getRecentItems();
        for (ItemSummary item : items) {
            model.addRow(new Object[]{
                    item.getTitle(),
                    item.getType() == null ? "" : item.getType().label(),
                    prettify(item.getCategory()),
                    item.getLocationLabel(),
                    item.getPostedByName(),
                    item.getStatus() == null ? "OPEN" : item.getStatus()});
        }

        List<String> missing = dashboardService.missingSegments();
        if (missing.isEmpty()) {
            hintLabel.setText("All four segments are connected — every card above is live data.");
            hintLabel.setForeground(UiTheme.MINT);
        } else {
            hintLabel.setText("Waiting for: " + String.join("  ·  ", missing)
                    + "   —   these cards fill in automatically once they register their service.");
            hintLabel.setForeground(UiTheme.AMBER);
        }
    }

    private String prettify(String category) {
        if (category == null || category.isBlank()) {
            return "-";
        }
        String value = category.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

}
