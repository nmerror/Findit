package com.bubt.findit.ui.auth;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.User;
import com.bubt.findit.ui.common.BaseFrame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/**
 * The registration screen - the same dark window as the login.
 *
 * <p>Only the e-mail address and the password are really needed. The student
 * ID is optional: when it is left empty, one is generated. Every new account
 * is a STUDENT - the manager account is created by the campus office and can
 * never be made by registering.
 */
public class RegisterFrame extends BaseFrame {

    private final AppServices services;
    private final AuthTextField nameField = new AuthTextField("full name");
    private final AuthTextField emailField = new AuthTextField("e-mail address, e.g. user234@gmail.com");
    private final AuthTextField studentIdField = new AuthTextField("student ID (optional)");
    private final AuthPasswordField passwordField = new AuthPasswordField("password (8+ characters, a letter and a digit)");
    private final AuthPasswordField confirmField = new AuthPasswordField("repeat the password");
    private final JLabel messageLabel = new JLabel(" ");

    public RegisterFrame(AppServices services) {
        super(services.config().appName(), "Create an account", 900, 640);
        this.services = services;

        getContent().setBackground(AuthColors.BACKGROUND);
        getContent().add(buildFormArea(), BorderLayout.CENTER);
        getContent().revalidate();
        getContent().repaint();
    }

    private JPanel buildFormArea() {
        JPanel area = new JPanel(new java.awt.GridBagLayout());
        area.setBackground(AuthColors.BACKGROUND);

        JPanel card = new JPanel();
        card.setBackground(AuthColors.CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(32, 36, 28, 36));
        card.setPreferredSize(new Dimension(430, 540));

        JLabel heading = new JLabel("Create your account");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        heading.setForeground(AuthColors.TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("It takes a minute - then you can report a lost item.");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        sub.setForeground(AuthColors.TEXT_MUTE);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));

        card.add(label("Full name"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(nameField));
        card.add(Box.createVerticalStrut(13));

        card.add(label("E-mail address"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(emailField));
        card.add(Box.createVerticalStrut(13));

        card.add(label("Student ID"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(studentIdField));
        card.add(Box.createVerticalStrut(13));

        card.add(label("Password"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(passwordField));
        card.add(Box.createVerticalStrut(13));

        card.add(label("Repeat the password"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(confirmField));
        card.add(Box.createVerticalStrut(8));

        messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        messageLabel.setForeground(AuthColors.ERROR);
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(8));

        AuthButton create = new AuthButton("Create account", AuthButton.Kind.PRIMARY);
        create.setAlignmentX(LEFT_ALIGNMENT);
        create.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        create.addActionListener(event -> register());
        card.add(create);
        card.add(Box.createVerticalStrut(14));

        JLabel back = new JLabel("I already have an account");
        back.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        back.setForeground(AuthColors.ACCENT);
        back.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        back.setAlignmentX(LEFT_ALIGNMENT);
        back.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                dispose();
            }
        });
        card.add(back);

        area.add(card);
        return area;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(AuthColors.TEXT_DIM);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JComponent fullWidth(JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return component;
    }

    private void register() {
        OperationResult<User> result = services.authService().register(
                nameField.getText(),
                emailField.getText(),
                studentIdField.getText(),
                new String(passwordField.getPassword()),
                new String(confirmField.getPassword()));

        if (result.isSuccess()) {
            messageLabel.setForeground(AuthColors.SUCCESS);
            messageLabel.setText(result.getMessage());
            passwordField.setText("");
            confirmField.setText("");
        } else {
            messageLabel.setForeground(AuthColors.ERROR);
            messageLabel.setText(result.getMessage());
        }
    }
}
