package com.bubt.findit.ui.users;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.User;
import com.bubt.findit.db.dao.AuditEntry;
import com.bubt.findit.ui.common.AvatarLabel;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.AppButton;
import com.bubt.findit.ui.common.ModernPasswordField;
import com.bubt.findit.ui.common.ModernTextField;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.StatusBadge;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

/**
 * "My Profile" of Segment 1: the account details on the left, the password
 * change on the right.
 */
public class ProfilePanel extends JPanel {

    private final AppServices services;
    private final User user;

    private final ModernTextField phoneField = new ModernTextField("e.g. 01700000000");
    private final ModernPasswordField currentPassword = new ModernPasswordField("current password");
    private final ModernPasswordField newPassword = new ModernPasswordField("new password");
    private final JLabel messageLabel = new JLabel(" ");

    public ProfilePanel(AppServices services, User user) {
        super(new BorderLayout());
        this.services = services;
        this.user = user;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0));
        columns.setOpaque(false);
        columns.add(buildIdentityCard());
        columns.add(buildSecurityCard());

        add(columns, BorderLayout.CENTER);
    }

    private JPanel buildIdentityCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        AvatarLabel avatar = new AvatarLabel(user.initials(),
                user.isAdmin() ? UiTheme.VIOLET : UiTheme.ACCENT, 62);

        JLabel name = new JLabel(user.getFullName());
        name.setFont(Fonts.display(22));
        name.setForeground(UiTheme.TEXT);

        StatusBadge status = new StatusBadge(user.getStatus().label(),
                UiTheme.accentFor(user.getStatus().name()));
        status.setMaximumSize(new Dimension(160, 24));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        name.setAlignmentX(LEFT_ALIGNMENT);
        status.setAlignmentX(LEFT_ALIGNMENT);
        texts.add(name);
        texts.add(Box.createVerticalStrut(8));
        texts.add(status);

        header.add(avatar, BorderLayout.WEST);
        header.add(texts, BorderLayout.CENTER);
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        card.add(header);
        card.add(Box.createVerticalStrut(22));

        card.add(field("E-mail address", user.getEmail()));
        card.add(Box.createVerticalStrut(14));
        card.add(field("Student ID", user.getStudentId()));
        card.add(Box.createVerticalStrut(14));
        card.add(field("Role", user.getRole().label()));
        card.add(Box.createVerticalStrut(14));
        card.add(field("Member since",
                user.getCreatedAt() == null ? "-" : user.getCreatedAt().toLocalDate().toString()));

        card.add(Box.createVerticalStrut(26));
        card.add(caption("RECENT ACTIVITY"));
        card.add(Box.createVerticalStrut(8));

        List<AuditEntry> entries = services.auditDao().byActor(user.getId(), 6);
        if (entries.isEmpty()) {
            JLabel none = new JLabel("No activity recorded yet.");
            none.setFont(Fonts.body(12));
            none.setForeground(UiTheme.TEXT_MUTE);
            none.setAlignmentX(LEFT_ALIGNMENT);
            card.add(none);
        } else {
            for (AuditEntry entry : entries) {
                card.add(activityRow(entry));
                card.add(Box.createVerticalStrut(6));
            }
        }

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JComponent field(String caption, String value) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JLabel captionLabel = new JLabel(caption.toUpperCase());
        captionLabel.setFont(Fonts.medium(10));
        captionLabel.setForeground(UiTheme.TEXT_MUTE);

        JLabel valueLabel = new JLabel(value == null ? "-" : value);
        valueLabel.setFont(Fonts.body(14));
        valueLabel.setForeground(UiTheme.TEXT);

        panel.add(captionLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    /** One line in the activity list: a coloured dot, the action and the time. */
    private JComponent activityRow(AuditEntry entry) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.ACCENT);
                g2.fillOval(0, 6, 7, 7);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(7, 20);
            }
        };
        dot.setOpaque(false);

        JLabel action = new JLabel(humanise(entry.getAction()));
        action.setFont(Fonts.body(12));
        action.setForeground(UiTheme.TEXT_DIM);

        JLabel time = new JLabel(entry.getCreatedAt() == null ? ""
                : entry.getCreatedAt().toLocalDate().toString());
        time.setFont(Fonts.caption(11));
        time.setForeground(UiTheme.TEXT_MUTE);

        row.add(dot, BorderLayout.WEST);
        row.add(action, BorderLayout.CENTER);
        row.add(time, BorderLayout.EAST);
        return row;
    }

    private String humanise(String action) {
        if (action == null || action.isBlank()) {
            return "-";
        }
        String lower = action.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private JPanel buildSecurityCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        JLabel heading = new JLabel("Contact & security");
        heading.setFont(Fonts.display(20));
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Keep your phone number up to date so the office can reach you.");
        sub.setFont(Fonts.body(12));
        sub.setForeground(UiTheme.TEXT_MUTE);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(20));

        card.add(caption("PHONE NUMBER"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(phoneField));
        card.add(Box.createVerticalStrut(10));

        AppButton save = new AppButton("Save phone number", AppButton.Kind.SECONDARY);
        save.setAlignmentX(LEFT_ALIGNMENT);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        save.addActionListener(event -> savePhone());
        card.add(save);

        card.add(Box.createVerticalStrut(26));

        card.add(caption("CHANGE PASSWORD"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(currentPassword));
        card.add(Box.createVerticalStrut(12));
        card.add(fullWidth(newPassword));
        card.add(Box.createVerticalStrut(10));

        AppButton change = new AppButton("Update password", AppButton.Kind.PRIMARY);
        change.setAlignmentX(LEFT_ALIGNMENT);
        change.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        change.addActionListener(event -> changePassword());
        card.add(change);
        card.add(Box.createVerticalStrut(14));

        messageLabel.setFont(Fonts.body(12));
        messageLabel.setForeground(UiTheme.MINT);
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(messageLabel);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.medium(10));
        label.setForeground(UiTheme.TEXT_MUTE);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JComponent fullWidth(JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return component;
    }

    private void savePhone() {
        OperationResult<User> result =
                services.userService().updateProfile(user, user.getId(), phoneField.getText());
        messageLabel.setForeground(result.isSuccess() ? UiTheme.MINT : UiTheme.ROSE);
        messageLabel.setText(result.getMessage());
    }

    private void changePassword() {
        OperationResult<User> result = services.authService().changePassword(user.getId(),
                new String(currentPassword.getPassword()),
                new String(newPassword.getPassword()));
        messageLabel.setForeground(result.isSuccess() ? UiTheme.MINT : UiTheme.ROSE);
        messageLabel.setText(result.getMessage());
        if (result.isSuccess()) {
            currentPassword.setText("");
            newPassword.setText("");
        }
    }
}
