package com.bubt.findit.ui.common;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The header of the content area: page title, live clock, search field and
 * the signed-in user.
 */
public class TopBar extends JPanel {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");

    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JLabel clockLabel = new JLabel();
    private final JLabel dateLabel = new JLabel();
    private final AvatarLabel avatar;

    public TopBar(String title, String subtitle, com.bubt.findit.core.model.User user,
                  String searchHint, java.util.function.Consumer<String> onSearch) {
        super(new BorderLayout());
        setBackground(UiTheme.WINDOW);
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.STROKE_SOFT),
                javax.swing.BorderFactory.createEmptyBorder(16, 24, 14, 24)));

        // ------------------------------------------------------------ left
        JPanel left = new JPanel(new BorderLayout(0, 3));
        left.setOpaque(false);

        titleLabel.setText(title);
        titleLabel.setFont(Fonts.display(24));
        titleLabel.setForeground(UiTheme.TEXT);

        subtitleLabel.setText(subtitle);
        subtitleLabel.setFont(Fonts.caption(12));
        subtitleLabel.setForeground(UiTheme.TEXT_MUTE);

        left.add(titleLabel, BorderLayout.NORTH);
        left.add(subtitleLabel, BorderLayout.SOUTH);

        // ----------------------------------------------------------- middle
        JPanel middle = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        middle.setOpaque(false);

        ModernTextField search = new ModernTextField(searchHint);
        search.setPreferredSize(new Dimension(260, 40));
        search.addActionListener(event -> {
            if (onSearch != null) {
                onSearch.accept(search.getText());
            }
        });
        // Search while typing - the results update with every keystroke.
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                fire(onSearch, search.getText());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                fire(onSearch, search.getText());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                fire(onSearch, search.getText());
            }

            private void fire(java.util.function.Consumer<String> consumer, String text) {
                if (consumer != null) {
                    consumer.accept(text);
                }
            }
        });

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.add(search, BorderLayout.CENTER);

        middle.add(searchWrap);

        // ----------------------------------------------------------- right
        JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        JPanel clockPanel = new JPanel(new java.awt.GridLayout(2, 1, 0, 0));
        clockPanel.setOpaque(false);
        clockLabel.setFont(Fonts.mono(14));
        clockLabel.setForeground(UiTheme.TEXT);
        clockLabel.setHorizontalAlignment(JLabel.RIGHT);
        dateLabel.setFont(Fonts.caption(11));
        dateLabel.setForeground(UiTheme.TEXT_MUTE);
        dateLabel.setHorizontalAlignment(JLabel.RIGHT);
        clockPanel.add(clockLabel);
        clockPanel.add(dateLabel);

        avatar = new AvatarLabel(user == null ? "?" : user.initials(),
                user != null && user.isAdmin() ? UiTheme.VIOLET : UiTheme.ACCENT, 36);

        right.add(clockPanel);
        right.add(avatar);

        add(left, BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        Timer timer = new Timer(1000, event -> {
            LocalDateTime now = LocalDateTime.now();
            clockLabel.setText(now.format(CLOCK));
            dateLabel.setText(now.format(DATE));
        });
        timer.start();
        clockLabel.setText(LocalDateTime.now().format(CLOCK));
        dateLabel.setText(LocalDateTime.now().format(DATE));

    }

    /** Changes the page title when the user navigates to another screen. */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
