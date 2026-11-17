package com.bubt.findit.ui.auth;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Session;
import com.bubt.findit.ui.MainFrame;
import com.bubt.findit.ui.common.BaseFrame;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.GradientBackdrop;
import com.bubt.findit.ui.common.GlowButton;
import com.bubt.findit.ui.common.Icons;
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * The login screen: a branded hero on the left, the sign-in card on the
 * right. The identifier accepts either the e-mail address or the student ID.
 */
public class LoginFrame extends BaseFrame {

    private final AppServices services;
    private final ModernTextField identifierField = new ModernTextField("e-mail address or student ID");
    private final ModernPasswordField passwordField = new ModernPasswordField("password");
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame(AppServices services) {
        super(services.config().appName(), "Campus Lost & Found Management System", 1080, 690);
        this.services = services;

        buildScreen();
    }

    /** (Re)builds the screen with the current palette. */
    private void buildScreen() {
        getContent().removeAll();
        getContent().setBackground(UiTheme.WINDOW);
        getContent().add(buildHero(), BorderLayout.WEST);
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

    // --------------------------------------------------------------- hero

    private JPanel buildHero() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setPreferredSize(new Dimension(520, 10));
        hero.setOpaque(false);

        hero.add(new GradientBackdrop(UiTheme.WINDOW_DEEP, UiTheme.WINDOW, UiTheme.ACCENT),
                BorderLayout.CENTER);

        JPanel overlay = new JPanel();
        overlay.setOpaque(false);
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        overlay.setBorder(javax.swing.BorderFactory.createEmptyBorder(54, 52, 44, 52));

        JLabel brand = new JLabel("FindIt");
        brand.setFont(Fonts.display(46));
        brand.setForeground(UiTheme.TEXT);
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html>"
                + "Lost something on campus? Found something that belongs to someone else? "
                + "FindIt matches the two, verifies the claim and returns the item to its owner."
                + "</html>");
        tagline.setFont(Fonts.body(14));
        tagline.setForeground(UiTheme.TEXT_DIM);
        tagline.setAlignmentX(LEFT_ALIGNMENT);
        tagline.setPreferredSize(new Dimension(400, 74));
        tagline.setMaximumSize(new Dimension(400, 74));

        overlay.add(brand);
        overlay.add(Box.createVerticalStrut(14));
        overlay.add(tagline);
        overlay.add(Box.createVerticalStrut(34));

        overlay.add(feature("Smart matching of lost and found items", UiTheme.ACCENT));
        overlay.add(Box.createVerticalStrut(12));
        overlay.add(feature("Photo evidence and admin verification", UiTheme.VIOLET));
        overlay.add(Box.createVerticalStrut(12));
        overlay.add(feature("Role based access for students and administrators", UiTheme.MINT));

        overlay.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("BUBT  ·  Java Project  ·  Swing + MySQL (JDBC)");
        footer.setFont(Fonts.caption(11));
        footer.setForeground(UiTheme.TEXT_MUTE);
        footer.setAlignmentX(LEFT_ALIGNMENT);
        overlay.add(footer);

        hero.add(overlay, BorderLayout.CENTER);
        return hero;
    }

    private JComponent feature(String text, Color accent) {
        JComponent row = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.alpha(accent, 28));
                g2.fillRoundRect(0, 2, 22, 22, 8, 8);
                Icons.draw(g2, Icons.Glyph.CHECK, 4, 5, 14, accent);
                g2.setColor(UiTheme.TEXT_DIM);
                g2.setFont(Fonts.body(13));
                g2.drawString(text, 32, 18);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(420, 26);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 26);
            }
        };
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }

    // --------------------------------------------------------------- form

    private JPanel buildFormArea() {
        JPanel area = new JPanel(new java.awt.GridBagLayout());
        area.setBackground(UiTheme.WINDOW);

        RoundedPanel card = new RoundedPanel(UiTheme.R_XL, UiTheme.SURFACE, UiTheme.STROKE);
        card.setGradient(UiTheme.CARD_GRADIENT);
        card.setGlow(UiTheme.ACCENT, 26);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(34, 38, 30, 38));
        card.setPreferredSize(new Dimension(400, 424));

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(Fonts.display(26));
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to continue to your dashboard.");
        sub.setFont(Fonts.body(13));
        sub.setForeground(UiTheme.TEXT_MUTE);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(26));

        card.add(label("E-mail or Student ID"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(identifierField));
        card.add(Box.createVerticalStrut(16));

        card.add(label("Password"));
        card.add(Box.createVerticalStrut(7));
        card.add(fullWidth(passwordField));
        card.add(Box.createVerticalStrut(10));

        messageLabel.setFont(Fonts.body(12));
        messageLabel.setForeground(UiTheme.ROSE);
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(8));

        GlowButton signIn = new GlowButton("Sign in", GlowButton.Kind.PRIMARY);
        signIn.setAlignmentX(LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        signIn.addActionListener(event -> performLogin());
        card.add(signIn);
        card.add(Box.createVerticalStrut(16));

        JLabel registerLink = linkLabel("Create a new student account", this::openRegisterFrame);
        card.add(registerLink);

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
