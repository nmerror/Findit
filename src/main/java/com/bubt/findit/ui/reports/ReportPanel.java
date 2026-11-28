package com.bubt.findit.ui.reports;

import com.bubt.findit.core.model.DashboardStats;
import com.bubt.findit.core.model.User;
import com.bubt.findit.db.dao.AuditDao;
import com.bubt.findit.db.dao.AuditEntry;
import com.bubt.findit.service.DashboardService;
import com.bubt.findit.service.ReportService;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.AppButton;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.List;

/**
 * The report screen (Section 6 - "Search and Report Generation").
 *
 * <p>Generates a printable summary of the whole system and exports it as a
 * text or CSV file. The numbers come from the dashboard service, so the
 * report automatically includes whatever the other segments have delivered.
 */
public class ReportPanel extends JPanel {

    private final DashboardService dashboardService;
    private final ReportService reportService;
    private final AuditDao auditDao;
    private final User viewer;

    private final JTextArea area = new JTextArea();
    private final JLabel status = new JLabel(" ");

    public ReportPanel(DashboardService dashboardService, ReportService reportService,
                       AuditDao auditDao, User viewer) {
        super(new BorderLayout(0, 14));
        this.dashboardService = dashboardService;
        this.reportService = reportService;
        this.auditDao = auditDao;
        this.viewer = viewer;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        JLabel heading = new JLabel("System summary");
        heading.setFont(Fonts.heading(15));
        heading.setForeground(UiTheme.TEXT);

        JLabel subtitle = new JLabel("Refresh it before every demo.");
        subtitle.setFont(Fonts.caption(12));
        subtitle.setForeground(UiTheme.TEXT_MUTE);

        JPanel head = new JPanel(new BorderLayout(12, 0));
        head.setOpaque(false);
        head.add(heading, BorderLayout.WEST);
        head.add(subtitle, BorderLayout.CENTER);

        area.setEditable(false);
        area.setFont(Fonts.mono(12));
        area.setForeground(UiTheme.TEXT_DIM);
        area.setBackground(UiTheme.SURFACE);
        area.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        area.setCaretColor(UiTheme.ACCENT);

        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout());
        card.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        AppButton refresh = new AppButton("Refresh", AppButton.Kind.PRIMARY);
        refresh.addActionListener(event -> refresh());

        AppButton exportText = new AppButton("Export as .txt", AppButton.Kind.SECONDARY);
        exportText.addActionListener(event -> export("txt"));

        AppButton exportCsv = new AppButton("Export as .csv", AppButton.Kind.SECONDARY);
        exportCsv.addActionListener(event -> export("csv"));

        toolbar.add(refresh);
        toolbar.add(exportText);
        toolbar.add(exportCsv);

        status.setFont(Fonts.body(12));
        status.setForeground(UiTheme.MINT);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(toolbar, BorderLayout.WEST);
        footer.add(status, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(head, BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);
        body.add(footer, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(700, 520));

        add(body, BorderLayout.CENTER);
        refresh();
    }

    /** Rebuilds the report text from the current data. */
    public void refresh() {
        DashboardStats stats = dashboardService.build(viewer);
        List<AuditEntry> audit = auditDao == null ? List.of() : auditDao.recent(25);
        area.setText(reportService.buildTextReport(viewer, stats, audit));
        area.setCaretPosition(0);
        status.setForeground(UiTheme.MINT);
        status.setText("Report updated");
    }

    private void export(String extension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(reportService.suggestedFileName(extension)));
        chooser.setDialogTitle("Save the FindIt report");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path target = chooser.getSelectedFile().toPath();
        String content = "csv".equals(extension)
                ? reportService.buildCsvReport(viewer, dashboardService.build(viewer))
                : area.getText();
        boolean ok = reportService.exportTo(target, content);
        if (ok) {
            reportService.logExport(viewer, extension);
            status.setForeground(UiTheme.MINT);
            status.setText("Saved to " + target.getFileName());
        } else {
            status.setForeground(UiTheme.ROSE);
            status.setText("Could not write " + target.getFileName());
            JOptionPane.showMessageDialog(this, "The report could not be saved.",
                    "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
