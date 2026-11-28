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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * The "forgot your password" screen.
 *
 * <p>Two steps: the user writes the e-mail address of the account and gets a
 * six digit code; then the code and the new password are written into the
 * second form and the password is changed.
 *
 * <p>FindIt has no mail server, so the code is shown on the screen (and
 * printed into the console) instead of being sent by e-mail. In a real
 * deployment this is the one line that would send the e-mail.
 */
public class ForgotPasswordFrame extends BaseFrame {

    private final AppServices services;
    private final CardLayout cards = new CardLayout();
    private final JPanel holder = new JPanel(cards);

    private final AuthTextField emailField = new AuthTextField("e-mail address of your account");
    private final AuthTextField pinField = new AuthTextField("the 6 digit code");
    private final AuthPasswordField passwordField = new AuthPasswordField("new password");
    private final AuthPasswordField confirmField = new AuthPasswordField("repeat the new password");

    private final JLabel emailMessage = new JLabel(" ");
    private final JLabel resetMessage = new JLabel(" ");
    private String email = "";

    public ForgotPasswordFrame(AppServices services) {
        super(services.config().appName(), "Reset your password", 900, 520);
        this.services = services;

        getContent().setBackground(AuthColors.BACKGROUND);
        holder.setBackground(AuthColors.BACKGROUND);
        holder.add(buildEmailStep(), "email");
        holder.add(buildResetStep(), "reset");
        getContent().add(holder, BorderLayout.CENTER);
        getContent().revalidate();
        getContent().repaint();
    }

    // ------------------------------------------------------------ step one

    private JPanel buildEmailStep() {
        JPanel card = new JPanel();
        card.setBackground(AuthColors.CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(34, 36, 30, 36));
        card.setPreferredSize(new Dimension(400, 360));

        card.add(heading("Reset your password"));
        card.add(Box.createVerticalStrut(6));
        card.add(hint("Write the e-mail address of your account - we send you a code."));
        card.add(Box.createVerticalStrut(24));

        card.add(label("E-mail address"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(emailField));
        card.add(Box.createVerticalStrut(10));

        emailMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        emailMessage.setForeground(AuthColors.ERROR);
        emailMessage.setAlignmentX(LEFT_ALIGNMENT);
        card.add(emailMessage);
        card.add(Box.createVerticalStrut(6));

        AuthButton send = new AuthButton("Send the code", AuthButton.Kind.PRIMARY);
        send.setAlignmentX(LEFT_ALIGNMENT);
        send.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        send.addActionListener(event -> requestCode());
        card.add(send);
        card.add(Box.createVerticalStrut(14));

        card.add(backLink("Back to the login", this::close));
        return card;
    }

    // ------------------------------------------------------------ step two

    private JPanel buildResetStep() {
        JPanel card = new JPanel();
        card.setBackground(AuthColors.CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(34, 36, 30, 36));
        card.setPreferredSize(new Dimension(400, 430));

        card.add(heading("Enter your code"));
        card.add(Box.createVerticalStrut(6));
        card.add(hint("The code is valid for ten minutes and can be used once."));
        card.add(Box.createVerticalStrut(14));

        JPanel codeBox = new JPanel(new BorderLayout());
        codeBox.setBackground(new Color(0x232830));
        codeBox.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(AuthColors.BORDER),
                javax.swing.BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel codeLabel = new JLabel(" ");
        codeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        codeLabel.setForeground(AuthColors.ACCENT);
        codeLabel.setName("codeLabel");
        codeBox.add(codeLabel, BorderLayout.CENTER);
        codeBox.setAlignmentX(LEFT_ALIGNMENT);
        codeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.add(codeBox);
        card.add(Box.createVerticalStrut(16));

        card.add(label("The code"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(pinField));
        card.add(Box.createVerticalStrut(13));

        card.add(label("New password"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(passwordField));
        card.add(Box.createVerticalStrut(13));

        card.add(label("Repeat the new password"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(confirmField));
        card.add(Box.createVerticalStrut(8));

        resetMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        resetMessage.setForeground(AuthColors.ERROR);
        resetMessage.setAlignmentX(LEFT_ALIGNMENT);
        card.add(resetMessage);
        card.add(Box.createVerticalStrut(6));

        AuthButton reset = new AuthButton("Save the new password", AuthButton.Kind.PRIMARY);
        reset.setAlignmentX(LEFT_ALIGNMENT);
        reset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        reset.addActionListener(event -> resetPassword());
        card.add(reset);
        card.add(Box.createVerticalStrut(14));

        card.add(backLink("Use a different e-mail address", this::backToEmail));
        return card;
    }

    // ------------------------------------------------------------- helpers

    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        label.setForeground(AuthColors.TEXT);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JLabel hint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(AuthColors.TEXT_MUTE);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
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

    private JLabel backLink(String text, Runnable action) {
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

    private void requestCode() {
        String address = emailField.getText().trim();
        OperationResult<String> result = services.authService().requestPasswordReset(address);
        if (!result.isSuccess()) {
            emailMessage.setForeground(AuthColors.ERROR);
            emailMessage.setText(result.getMessage());
            return;
        }
        email = address;
        emailMessage.setForeground(AuthColors.SUCCESS);
        emailMessage.setText(result.getMessage());

        // Show the code - in a real deployment it would arrive by e-mail.
        for (java.awt.Component component : findCodeLabel(holder)) {
            JLabel codeLabel = (JLabel) component;
            codeLabel.setText("Your code:  " + result.getData());
        }
        cards.show(holder, "reset");
        holder.revalidate();
        holder.repaint();
    }

    private void resetPassword() {
        String newPassword = new String(passwordField.getPassword());
        String repeat = new String(confirmField.getPassword());
        if (!newPassword.equals(repeat)) {
            resetMessage.setForeground(AuthColors.ERROR);
            resetMessage.setText("The two passwords do not match.");
            return;
        }
        OperationResult<User> result = services.authService()
                .resetPassword(email, pinField.getText(), newPassword);
        if (result.isSuccess()) {
            resetMessage.setForeground(AuthColors.SUCCESS);
            resetMessage.setText(result.getMessage());
            passwordField.setText("");
            confirmField.setText("");
        } else {
            resetMessage.setForeground(AuthColors.ERROR);
            resetMessage.setText(result.getMessage());
        }
    }

    private void backToEmail() {
        cards.show(holder, "email");
        holder.revalidate();
        holder.repaint();
    }

    private void close() {
        dispose();
    }

    /** Finds the label that shows the code. */
    private java.util.List<java.awt.Component> findCodeLabel(java.awt.Container container) {
        java.util.List<java.awt.Component> found = new java.util.ArrayList<>();
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof JLabel label && "codeLabel".equals(label.getName())) {
                found.add(label);
            } else if (component instanceof java.awt.Container child) {
                found.addAll(findCodeLabel(child));
            }
        }
        return found;
    }
}
