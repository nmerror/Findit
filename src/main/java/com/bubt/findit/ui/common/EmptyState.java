package com.bubt.findit.ui.common;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;

/**
 * The message that is shown instead of an empty table.
 *
 * <p>FindIt is a real system: until somebody posts a lost item, the board is
 * simply empty - and it says so.
 */
public class EmptyState extends JPanel {

    public EmptyState(String title, String hint) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(Fonts.heading(15));
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(CENTER_ALIGNMENT);

        JLabel text = new JLabel(hint);
        text.setFont(Fonts.body(13));
        text.setForeground(UiTheme.TEXT_MUTE);
        text.setAlignmentX(CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(heading);
        add(Box.createVerticalStrut(6));
        add(text);
        add(Box.createVerticalGlue());
        setPreferredSize(new Dimension(200, 90));
    }
}
