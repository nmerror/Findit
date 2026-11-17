package com.bubt.findit.ui.auth;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.User;
import com.bubt.findit.ui.common.BaseFrame;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.GlowButton;
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
import java.awt.Color;
import java.awt.Dimension;

/** Registration screen - only student accounts can be created here. */
public class RegisterFrame extends BaseFrame {

    private final AppServices services;
    private final ModernTextField nameField = new ModernTextField("e.g. Ayesha Rahman");
    private final ModernTextField emailField = new ModernTextField("e.g. ayesha@buet.edu");
    private final ModernTextField studentIdField = new ModernTextField("e.g. 21-04234");
    private final ModernPasswordField passwordField = new ModernPasswordField("at least 8 characters");
    private final ModernPasswordField confirmField = new ModernPasswordField("repeat the password");
    private final JLabel messageLabel = new JLabel(" ");

    public RegisterFrame(AppServices services) {
        super(services.config().appName(), "Create a student account", 720, 760);
        this.services = services;

        buildScreen();
    }

    /** (Re)builds the screen with the current palette. */
    private void buildScreen() {
        getContent().removeAll();
        getContent().setBackground(UiTheme.WINDOW);
        getContent().add(buildFormArea(), BorderLayout.CENTER);
        getContent().revalidate();
        getContent().repaint();
    }

    @Override
    protected void onThemeChanged() {
        buildScreen();
    }

    @Override
    protected void onThemePersisted() {
        services.config().saveTheme(UiTheme.mode().name().toLowerCase());
    }

    private JPanel buildFormArea() {
        JPanel area = new JPanel(new java.awt.GridBagLayout());
        area.setBackground(UiTheme.WINDOW);

        RoundedPanel card = new RoundedPanel(UiTheme.R_XL, UiTheme.SURFACE, UiTheme.STROKE);
        card.setGradient(UiTheme.CARD_GRADIENT);
        card.setGlow(UiTheme.VIOLET, 24);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(32, 36, 28, 36));
        card.setPreferredSize(new Dimension(430, 620));

        JLabel heading = new JLabel("Create your account");
        heading.setFont(Fonts.display(26));
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Administrator accounts are created by the campus office.");
        sub.setFont(Fonts.body(12));
        sub.setForeground(UiTheme.TEXT_MUTE);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));

        card.add(label("Full name"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(nameField));
        card.add(Box.createVerticalStrut(14));

        card.add(label("E-mail address"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(emailField));
        card.add(Box.createVerticalStrut(14));

        card.add(label("Student ID"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(studentIdField));
        card.add(Box.createVerticalStrut(14));

        card.add(label("Password"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(passwordField));
        card.add(Box.createVerticalStrut(14));

        card.add(label("Confirm password"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(confirmField));
        card.add(Box.createVerticalStrut(8));

        messageLabel.setFont(Fonts.body(12));
        messageLabel.setForeground(UiTheme.ROSE);
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(10));

        GlowButton create = new GlowButton("Create account", GlowButton.Kind.PRIMARY);
        create.setAccent(UiTheme.VIOLET);
        create.setAlignmentX(LEFT_ALIGNMENT);
        create.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        create.addActionListener(event -> performRegister());
        card.add(create);

        area.add(card);
        return area;
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
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return component;
    }

    private void performRegister() {
        OperationResult<User> result = services.authService().register(
                nameField.getText(),
                emailField.getText(),
                studentIdField.getText(),
                new String(passwordField.getPassword()),
                new String(confirmField.getPassword()));

        if (result.isSuccess()) {
            messageLabel.setForeground(UiTheme.MINT);
            messageLabel.setText(result.getMessage() + "  You can sign in now.");
            nameField.setText("");
            emailField.setText("");
            studentIdField.setText("");
            passwordField.setText("");
            confirmField.setText("");
        } else {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText(result.getMessage());
        }
    }
}
