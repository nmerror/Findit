package com.bubt.findit.ui.users;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.User;
import com.bubt.findit.db.dao.AuditEntry;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.GlowButton;
import com.bubt.findit.ui.common.ModernTable;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * "Audit Log" of Segment 1 (administrators only).
 *
 * <p>Every segment writes into the same table through the {@code AuditService}
 * contract, so this screen shows the history of the whole system.
 */
public class AuditLogPanel extends JPanel {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AppServices services;
    private final User actor;
    private final JLabel status = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"When", "User", "Action", "Details"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public AuditLogPanel(AppServices services, User actor) {
        super(new BorderLayout(0, 16));
        this.services = services;
        this.actor = actor;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        JTable table = ModernTable.style(new JTable(model));
        int[] widths = {170, 70, 190, 420};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(widths[i] / 2);
        }

        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("EVERY ACTION IS RECORDED");
        title.setFont(Fonts.medium(11));
        title.setForeground(UiTheme.TEXT_MUTE);
        header.add(title, BorderLayout.WEST);

        GlowButton refresh = new GlowButton("Refresh", GlowButton.Kind.PRIMARY);
        refresh.addActionListener(event -> reload());

        GlowButton clear = new GlowButton("Clear entries older than 30 days", GlowButton.Kind.GHOST);
        clear.setAccent(UiTheme.ROSE);
        clear.addActionListener(event -> clearOldEntries());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(clear);
        right.add(refresh);
        header.add(right, BorderLayout.EAST);

        status.setFont(Fonts.body(12));
        status.setForeground(UiTheme.TEXT_DIM);
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        statusRow.add(status, BorderLayout.WEST);
        header.add(statusRow, BorderLayout.SOUTH);

        JScrollPane scroll = ModernTable.scroll(table);
        scroll.setPreferredSize(new Dimension(700, 420));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        reload();
    }

    /** DELETE operation on the audit trail. */
    private void clearOldEntries() {
        int answer = javax.swing.JOptionPane.showConfirmDialog(this,
                "Delete every audit entry older than 30 days?",
                "Confirm delete", javax.swing.JOptionPane.YES_NO_OPTION);
        if (answer != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        OperationResult<Integer> result = services.userService().clearOldAuditEntries(actor, 30);
        status.setForeground(result.isSuccess() ? UiTheme.MINT : UiTheme.ROSE);
        status.setText(result.getMessage());
        reload();
    }

    public void reload() {
        model.setRowCount(0);
        List<AuditEntry> entries = services.auditDao().recent(200);
        for (AuditEntry entry : entries) {
            model.addRow(new Object[]{
                    entry.getCreatedAt() == null ? "" : entry.getCreatedAt().format(FORMAT),
                    entry.getActorUserId() == null ? "system" : entry.getActorUserId(),
                    entry.getAction(),
                    entry.getDetails()});
        }
    }
}
