package com.bubt.findit.ui.auth;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Session;
import com.bubt.findit.ui.MainFrame;
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
 * The login screen - the entrance of the application.
 *
 * <p>Black, white and grey: a dark window with one form in the middle. The
 * identifier accepts the e-mail address (for example {@code user234@gmail.com})
 * or the student ID. The link below the button opens the registration, the
 * second link the password reset.
 */
public class LoginFrame extends BaseFrame {

    private final AppServices services;
    private final AuthTextField identifierField = new AuthTextField("e-mail address or student ID");
    private final AuthPasswordField passwordField = new AuthPasswordField("password");
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame(AppServices services) {
        super(services.config().appName(), "Campus Lost & Found Management System", 900, 580);
        this.services = services;

        getContent().setBackground(AuthColors.BACKGROUND);
        getContent().add(buildFormArea(), BorderLayout.CENTER);
        getContent().revalidate();
        getContent().repaint();
    }

    // --------------------------------------------------------------- form

    private JPanel buildFormArea() {
        JPanel area = new JPanel(new java.awt.GridBagLayout());
        area.setBackground(AuthColors.BACKGROUND);

        JPanel card = new JPanel();
        card.setBackground(AuthColors.CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(34, 36, 30, 36));
        card.setPreferredSize(new Dimension(400, 430));

        JLabel brand = new JLabel(services.config().appName());
        brand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        brand.setForeground(AuthColors.TEXT);
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Campus Lost & Found Management System");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        sub.setForeground(AuthColors.TEXT_MUTE);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(brand);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(26));

        card.add(label("E-mail or Student ID"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(identifierField));
        card.add(Box.createVerticalStrut(15));

        card.add(label("Password"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(passwordField));
        card.add(Box.createVerticalStrut(8));

        messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        messageLabel.setForeground(AuthColors.ERROR);
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(6));

        AuthButton signIn = new AuthButton("Sign in", AuthButton.Kind.PRIMARY);
        signIn.setAlignmentX(LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        signIn.addActionListener(event -> performLogin());
        card.add(signIn);
        card.add(Box.createVerticalStrut(16));

        card.add(linkLabel("Create a new account", this::openRegisterFrame));
        card.add(Box.createVerticalStrut(6));
        card.add(linkLabel("Forgot your password?", this::openResetFrame));

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

    private JLabel linkLabel(String text, Runnable action) {
        JLabel link = new JLabel(text);
        link.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        link.setForeground(AuthColors.ACCENT);
        link.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        link.setAlignmentX(LEFT_ALIGNMENT);
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                action.run();
            }
        });
        return link;
    }

    // ------------------------------------------------------------- actions

    private void performLogin() {
        String identifier = identifierField.getText();
        String password = new String(passwordField.getPassword());
        OperationResult<Session> result = services.authService().login(identifier, password);

        if (result.isSuccess()) {
            dispose();
            new MainFrame(services).setVisible(true);
        } else {
            messageLabel.setForeground(AuthColors.ERROR);
            messageLabel.setText(result.getMessage());
            passwordField.setText("");
        }
    }

    private void openRegisterFrame() {
        new RegisterFrame(services).setVisible(true);
    }

    private void openResetFrame() {
        new ForgotPasswordFrame(services).setVisible(true);
    }
}
