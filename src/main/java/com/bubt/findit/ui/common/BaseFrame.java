package com.bubt.findit.ui.common;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * An undecorated window with a custom title bar.
 *
 * <p>Using our own title bar removes the last trace of the default Swing
 * look: the whole window becomes one dark, designed surface, with an accent
 * line, an application mark, our own window buttons and the dark / light
 * switch.
 */
public abstract class BaseFrame extends JFrame {

    protected final JPanel content = new JPanel(new BorderLayout());
    private final JPanel root = new JPanel(new BorderLayout());
    private final JPanel titleBarHolder = new JPanel(new BorderLayout());

    private final String title;
    private final String subtitle;

    protected BaseFrame(String title, String subtitle, int width, int height) {
        this.title = title;
        this.subtitle = subtitle;

        setUndecorated(true);
        setSize(width, height);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(820, 560));

        root.setBackground(UiTheme.WINDOW);
        root.setLayout(new BorderLayout());
        titleBarHolder.setBackground(UiTheme.WINDOW_DEEP);
        root.add(titleBarHolder, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        rebuildChrome();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                onCloseRequested();
            }
        });
    }

    /** Called when the user presses the custom close button. */
    protected void onCloseRequested() {
        dispose();
        System.exit(0);
    }

    /** The panel every screen puts its content into. */
    protected JPanel getContent() {
        return content;
    }

    /**
     * Rebuilds the title bar. Call it after a theme change, because the
     * colours of the bar are fixed when it is created.
     */
    protected void rebuildChrome() {
        titleBarHolder.removeAll();
        titleBarHolder.add(buildTitleBar(), BorderLayout.CENTER);
        titleBarHolder.revalidate();
        titleBarHolder.repaint();
        root.setBackground(UiTheme.WINDOW);
        content.setBackground(UiTheme.WINDOW);
    }

    /**
     * Called after the palette changed so the subclass can rebuild its
     * screens with the new colours.
     */
    protected void onThemeChanged() {
        // Subclasses override this.
    }

    /** Called after the palette changed - used to remember the choice. */
    protected void onThemePersisted() {
        // Subclasses override this.
    }

    /** Rebuilds the window for the new palette. */
    private void handleThemeSwitch() {
        rebuildChrome();
        onThemeChanged();
        onThemePersisted();
    }

    private JComponent buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UiTheme.WINDOW_DEEP);
        bar.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 18, 10, 14));

        // --- mark + title -------------------------------------------------
        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);

        JComponent mark = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                g2.setPaint(new java.awt.GradientPaint(0, 0, UiTheme.ACCENT, size, size, UiTheme.VIOLET));
                g2.fillRoundRect(0, 0, size, size, 10, 10);
                g2.setColor(UiTheme.ON_ACCENT);
                g2.setFont(Fonts.display(14));
                java.awt.FontMetrics metrics = g2.getFontMetrics();
                String letter = "F";
                g2.drawString(letter, (size - metrics.stringWidth(letter)) / 2,
                        (size + metrics.getAscent() - metrics.getDescent()) / 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(28, 28);
            }
        };

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.heading(14));
        titleLabel.setForeground(UiTheme.TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(Fonts.caption(12));
        subtitleLabel.setForeground(UiTheme.TEXT_MUTE);

        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(titleLabel, BorderLayout.NORTH);
        titles.add(subtitleLabel, BorderLayout.SOUTH);

        left.add(mark, BorderLayout.WEST);
        left.add(titles, BorderLayout.CENTER);

        // --- window buttons ----------------------------------------------
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(new ThemeToggle(this::handleThemeSwitch));
        buttons.add(windowButton("–", false, event -> setState(java.awt.Frame.ICONIFIED)));
        buttons.add(windowButton("✕", true, event -> onCloseRequested()));

        bar.add(left, BorderLayout.CENTER);
        bar.add(buttons, BorderLayout.EAST);

        // --- accent line -------------------------------------------------
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new java.awt.GradientPaint(0, 0, UiTheme.ACCENT,
                        getWidth(), 0, UiTheme.VIOLET));
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(10, 2));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiTheme.WINDOW_DEEP);
        wrapper.add(bar, BorderLayout.CENTER);
        wrapper.add(line, BorderLayout.SOUTH);

        makeDraggable(wrapper);
        return wrapper;
    }

    private JButton windowButton(String glyph, boolean danger, java.util.function.Consumer<MouseEvent> action) {
        JButton button = new JButton(glyph) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed() || getModel().isRollover()) {
                    g2.setColor(danger ? UiTheme.alpha(UiTheme.ROSE, 60) : UiTheme.alpha(UiTheme.ACCENT, 50));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(danger ? UiTheme.ROSE : UiTheme.TEXT_DIM);
                g2.setFont(Fonts.medium(13));
                java.awt.FontMetrics metrics = g2.getFontMetrics();
                g2.drawString(glyph, (getWidth() - metrics.stringWidth(glyph)) / 2,
                        (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2);
                g2.dispose();
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(30, 28));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                action.accept(event);
            }
        });
        return button;
    }

    /** Lets the user drag the window by its title bar. */
    private void makeDraggable(Component component) {
        final Point press = new Point();
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                press.setLocation(event.getXOnScreen(), event.getYOnScreen());
            }
        });
        component.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                int x = event.getXOnScreen() - press.x;
                int y = event.getYOnScreen() - press.y;
                setLocation(x, y);
            }
        });
    }
}
