package com.bubt.findit.ui.auth;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Session;
import com.bubt.findit.ui.MainFrame;
import com.bubt.findit.ui.common.AppButton;
import com.bubt.findit.ui.common.BaseFrame;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.ModernPasswordField;
import com.bubt.findit.ui.common.ModernTextField;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * The login screen: one form in the middle of the window.
 *
 * <p>The identifier accepts either the e-mail address or the student ID. When
 * the credentials are correct the main window opens, otherwise the message
 * under the fields explains what went wrong.
 */
public class LoginFrame extends BaseFrame {

    private final AppServices services;
    private final ModernTextField identifierField = new ModernTextField("e-mail address or student ID");
    private final ModernPasswordField passwordField = new ModernPasswordField("password");
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame(AppServices services) {
        super(services.config().appName(), "Campus Lost & Found Management System", 880, 560);
        this.services = services;

        buildScreen();
    }

    private void buildScreen() {
        getContent().removeAll();
        getContent().setBackground(UiTheme.WINDOW);
        getContent().add(buildFormArea(), BorderLayout.CENTER);
        getContent().add(buildFooter(), BorderLayout.SOUTH);
        getContent().revalidate();
        getContent().repaint();
    }

    // --------------------------------------------------------------- form

    private JPanel buildFormArea() {
        JPanel area = new JPanel(new java.awt.GridBagLayout());
        area.setBackground(UiTheme.WINDOW);

        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 34, 26, 34));
        card.setPreferredSize(new Dimension(400, 378));

        JLabel brand = new JLabel(services.config().appName());
        brand.setFont(Fonts.display(24));
        brand.setForeground(UiTheme.TEXT);
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Campus Lost & Found Management System");
        sub.setFont(Fonts.body(12));
        sub.setForeground(UiTheme.TEXT_MUTE);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(brand);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));

        card.add(label("E-mail or Student ID"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(identifierField));
        card.add(Box.createVerticalStrut(14));

        card.add(label("Password"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(passwordField));
        card.add(Box.createVerticalStrut(8));

        messageLabel.setFont(Fonts.body(12));
        messageLabel.setForeground(UiTheme.ROSE);
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(8));

        AppButton signIn = new AppButton("Sign in", AppButton.Kind.PRIMARY);
        signIn.setAlignmentX(LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        signIn.addActionListener(event -> performLogin());
        card.add(signIn);
        card.add(Box.createVerticalStrut(14));

        JLabel registerLink = linkLabel("Create a new student account", this::openRegisterFrame);
        card.add(registerLink);

        area.add(card);
        return area;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel note = new JLabel("BUBT  ·  Advanced Programming Lab (CSE 342)  ·  Swing + MySQL (JDBC)");
        note.setFont(Fonts.caption(11));
        note.setForeground(UiTheme.TEXT_MUTE);
        note.setHorizontalAlignment(JLabel.CENTER);
        footer.add(note);
        return footer;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.medium(12));
        label.setForeground(UiTheme.TEXT_DIM);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JComponent fullWidth(JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return component;
    }

    private JLabel linkLabel(String text, Runnable action) {
        JLabel link = new JLabel(text);
        link.setFont(Fonts.medium(12));
        link.setForeground(UiTheme.ACCENT);
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

    private void performLogin() {
        String identifier = identifierField.getText();
        String password = new String(passwordField.getPassword());
        OperationResult<Session> result = services.authService().login(identifier, password);

        if (result.isSuccess()) {
            dispose();
            new MainFrame(services).setVisible(true);
        } else {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText(result.getMessage());
            passwordField.setText("");
        }
    }

    private void openRegisterFrame() {
        new RegisterFrame(services).setVisible(true);
    }
}
