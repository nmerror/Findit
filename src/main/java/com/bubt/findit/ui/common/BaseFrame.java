package com.bubt.findit.ui.common;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The base class of every window of the application.
 *
 * <p>It keeps the three things that all windows share: the title, the size and
 * the panel the screen puts its content into. The window uses the normal
 * decorations of the operating system - no custom title bar.
 *
 * <p>{@code LoginFrame}, {@code RegisterFrame} and {@code MainFrame} all extend
 * this class (inheritance - one of the OOP concepts of the project proposal).
 */
public abstract class BaseFrame extends JFrame {

    protected final JPanel content = new JPanel(new BorderLayout());

    private final String title;
    private final String subtitle;

    protected BaseFrame(String title, String subtitle, int width, int height) {
        this.title = title;
        this.subtitle = subtitle;

        setTitle(title + " - " + subtitle);
        setSize(width, height);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        content.setBackground(UiTheme.WINDOW);
        setContentPane(content);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                onCloseRequested();
            }
        });
    }

    /** The title of the window. */
    protected String windowTitle() {
        return title;
    }

    /** The subtitle of the window. */
    protected String windowSubtitle() {
        return subtitle;
    }

    /** Called when the user closes the window. */
    protected void onCloseRequested() {
        dispose();
        System.exit(0);
    }

    /** The panel every screen puts its content into. */
    protected JPanel getContent() {
        return content;
    }
}
