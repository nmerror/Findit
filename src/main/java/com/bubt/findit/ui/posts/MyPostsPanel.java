package com.bubt.findit.ui.posts;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.User;
import com.bubt.findit.service.PostService;
import com.bubt.findit.ui.common.AppButton;
import com.bubt.findit.ui.common.EmptyState;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.ModernTable;
import com.bubt.findit.ui.common.PhotoRenderer;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The posts of the signed-in student.
 *
 * <p>When the student got the item back, the button <em>Confirm found</em>
 * closes the post - and the post disappears from the board.
 */
public class MyPostsPanel extends JPanel {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final PostService postService;
    private final User user;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Photo", "Item", "Category", "Where", "Posted on"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = ModernTable.style(new JTable(model));
    private final java.util.Map<Integer, Long> ids = new java.util.LinkedHashMap<>();
    private final CardLayout cards = new CardLayout();
    private final JPanel area = new JPanel(cards);
    private final JLabel messageLabel = new JLabel(" ");
    private final AppButton confirmButton = new AppButton("Confirm found", AppButton.Kind.PRIMARY);

    public MyPostsPanel(AppServices services, User user) {
        super(new BorderLayout());
        this.postService = services.postService();
        this.user = user;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        add(buildCard(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("MY POSTS");
        title.setFont(Fonts.medium(11));
        title.setForeground(UiTheme.TEXT_MUTE);
        card.add(title, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {64, 210, 155, 170, 160};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(0).setCellRenderer(new PhotoRenderer());

        area.setOpaque(false);
        area.add(new EmptyState("You have not posted anything yet",
                "Report a lost item and it will show up here."), "empty");
        area.add(new JScrollPane(table), "table");
        card.add(area, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);

        AppButton confirm = confirmButton;
        confirm.addActionListener(event -> confirmFound());

        messageLabel.setFont(Fonts.body(12));
        messageLabel.setForeground(UiTheme.TEXT_DIM);

        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(confirm);
        footer.add(left, BorderLayout.WEST);
        footer.add(messageLabel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    /** Reads the posts of the user again. */
    public void refresh() {
        List<ItemSummary> posts = postService.myPosts(user.getId(), 100);
        model.setRowCount(0);
        ids.clear();
        for (ItemSummary post : posts) {
            ids.put(model.getRowCount(), post.getId());
            model.addRow(new Object[]{
                    post.getImagePath(),
                    post.getTitle(),
                    com.bubt.findit.core.model.ItemCategory.fromString(post.getCategory()).label(),
                    post.getLocationLabel(),
                    post.getCreatedAt() == null ? "" : post.getCreatedAt().format(DATE)});
        }
        cards.show(area, posts.isEmpty() ? "empty" : "table");
        // The button only makes sense when the student still has an open post.
        confirmButton.setVisible(!posts.isEmpty());
    }

    /** The owner confirms that the item was found - the post is closed. */
    private void confirmFound() {
        int row = table.getSelectedRow();
        if (row < 0) {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText("Select one of your posts first.");
            return;
        }
        long itemId = itemIdOf(row);
        String title = String.valueOf(model.getValueAt(row, 1));
        int answer = JOptionPane.showConfirmDialog(this,
                "Did you get \"" + title + "\" back?\nThe post will be removed from the board.",
                "Confirm found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        OperationResult<String> result = postService.confirmFound(user, itemId);
        if (result.isSuccess()) {
            messageLabel.setForeground(UiTheme.MINT);
            messageLabel.setText(result.getMessage());
            refresh();
        } else {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText(result.getMessage());
        }
    }

    /** The id of the post in the given row. */
    private long itemIdOf(int row) {
        Long id = ids.get(row);
        return id == null ? 0 : id;
    }
}
