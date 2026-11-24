package com.bubt.findit.ui.users;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.GlowButton;
import com.bubt.findit.ui.common.ModernTable;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.StatusTableRenderer;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

/**
 * "Manage Users" of Segment 1 (administrators only).
 *
 * <p>The navigation only shows this screen to administrators, and every action
 * is checked again inside {@code UserService} - the UI is not the security
 * boundary, the service is.
 */
public class UserManagementPanel extends JPanel {

    private final AppServices services;
    private final User actor;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "E-mail", "Student ID", "Role", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table;
    private final JLabel messageLabel = new JLabel(" ");

    public UserManagementPanel(AppServices services, User actor) {
        super(new BorderLayout(0, 16));
        this.services = services;
        this.actor = actor;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        table = ModernTable.style(new JTable(model));
        int[] widths = {60, 200, 230, 130, 130, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusTableRenderer());

        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        GlowButton suspend = new GlowButton("Suspend", GlowButton.Kind.GHOST);
        suspend.setAccent(UiTheme.ROSE);
        suspend.addActionListener(event -> changeStatus(UserStatus.SUSPENDED));

        GlowButton restore = new GlowButton("Restore", GlowButton.Kind.GHOST);
        restore.setAccent(UiTheme.MINT);
        restore.addActionListener(event -> changeStatus(UserStatus.ACTIVE));

        GlowButton makeAdmin = new GlowButton("Grant admin", GlowButton.Kind.GHOST);
        makeAdmin.setAccent(UiTheme.VIOLET);
        makeAdmin.addActionListener(event -> changeRole(Role.ADMIN));

        GlowButton makeStudent = new GlowButton("Revoke admin", GlowButton.Kind.GHOST);
        makeStudent.addActionListener(event -> changeRole(Role.STUDENT));

        GlowButton refresh = new GlowButton("Refresh", GlowButton.Kind.PRIMARY);
        refresh.addActionListener(event -> reload());

        toolbar.add(suspend);
        toolbar.add(restore);
        toolbar.add(makeAdmin);
        toolbar.add(makeStudent);
        toolbar.add(refresh);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("REGISTERED ACCOUNTS");
        title.setFont(Fonts.medium(11));
        title.setForeground(UiTheme.TEXT_MUTE);
        header.add(title, BorderLayout.WEST);
        messageLabel.setFont(Fonts.body(12));
        messageLabel.setForeground(UiTheme.TEXT_DIM);
        header.add(messageLabel, BorderLayout.EAST);

        JScrollPane scroll = ModernTable.scroll(table);
        scroll.setPreferredSize(new Dimension(700, 380));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(toolbar, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
        reload();
    }

    public void reload() {
        model.setRowCount(0);
        List<User> users;
        try {
            users = services.userService().listUsers(actor, 200, 0);
        } catch (com.bubt.findit.service.AccessDeniedException ex) {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText(ex.getMessage());
            return;
        }
        for (User user : users) {
            model.addRow(new Object[]{
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getStudentId(),
                    user.getRole().label(),
                    user.getStatus().label()});
        }
    }

    private Long selectedUserId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText("Select a user first.");
            return null;
        }
        Object value = model.getValueAt(row, 0);
        return value == null ? null : Long.valueOf(value.toString());
    }

    private void changeStatus(UserStatus status) {
        Long userId = selectedUserId();
        if (userId == null) {
            return;
        }
        OperationResult<User> result = status == UserStatus.SUSPENDED
                ? services.userService().suspend(actor, userId)
                : services.userService().restore(actor, userId);
        messageLabel.setForeground(result.isSuccess() ? UiTheme.MINT : UiTheme.ROSE);
        messageLabel.setText(result.getMessage());
        reload();
    }

    private void changeRole(Role role) {
        Long userId = selectedUserId();
        if (userId == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Change the role of this user to " + role.label() + "?",
                "Confirm role change", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        OperationResult<User> result = services.userService().changeRole(actor, userId, role);
        messageLabel.setForeground(result.isSuccess() ? UiTheme.MINT : UiTheme.ROSE);
        messageLabel.setText(result.getMessage());
        reload();
    }

}
