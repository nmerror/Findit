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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The administrator view of every lost item on the board.
 *
 * <p>The office can remove a post here - for example when the item was
 * handed over and the student forgot to close the post.
 */
public class PostManagementPanel extends JPanel {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final PostService postService;
    private final User admin;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Photo", "Item", "Category", "Where", "Posted by", "Posted on", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final Map<Integer, Long> ids = new LinkedHashMap<>();
    private final JTable table = ModernTable.style(new JTable(model));
    private final CardLayout cards = new CardLayout();
    private final JPanel area = new JPanel(cards);
    private final JLabel messageLabel = new JLabel(" ");
    private final AppButton removeButton = new AppButton("Remove the post", AppButton.Kind.DANGER);

    public PostManagementPanel(AppServices services, User admin) {
        super(new BorderLayout());
        this.postService = services.postService();
        this.admin = admin;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        add(buildCard(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("ALL POSTS");
        title.setFont(Fonts.medium(11));
        title.setForeground(UiTheme.TEXT_MUTE);
        card.add(title, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {64, 175, 155, 150, 150, 150, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(0).setCellRenderer(new PhotoRenderer());

        area.setOpaque(false);
        area.add(new EmptyState("No posts yet", "The board is empty - nothing has been reported."),
                "empty");
        area.add(new JScrollPane(table), "table");
        card.add(area, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);

        AppButton remove = removeButton;
        remove.addActionListener(event -> removePost());

        messageLabel.setFont(Fonts.body(12));
        messageLabel.setForeground(UiTheme.TEXT_DIM);

        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(remove);
        footer.add(left, BorderLayout.WEST);
        footer.add(messageLabel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    /** Reads every post again. */
    public void refresh() {
        List<ItemSummary> posts = postService.allPosts(200);
        model.setRowCount(0);
        ids.clear();
        for (ItemSummary post : posts) {
            ids.put(model.getRowCount(), post.getId());
            model.addRow(new Object[]{
                    post.getImagePath(),
                    post.getTitle(),
                    com.bubt.findit.core.model.ItemCategory.fromString(post.getCategory()).label(),
                    post.getLocationLabel(),
                    post.getPostedByName(),
                    post.getCreatedAt() == null ? "" : post.getCreatedAt().format(DATE),
                    post.getStatus()});
        }
        cards.show(area, posts.isEmpty() ? "empty" : "table");
        // Nothing to remove while the board is empty.
        removeButton.setVisible(!posts.isEmpty());
    }

    private void removePost() {
        int row = table.getSelectedRow();
        if (row < 0) {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText("Select a post first.");
            return;
        }
        String title = String.valueOf(model.getValueAt(row, 1));
        int answer = JOptionPane.showConfirmDialog(this,
                "Remove the post \"" + title + "\" from the board?",
                "Remove the post", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        Long id = ids.get(row);
        OperationResult<String> result = postService.deletePost(admin, id == null ? 0 : id);
        if (result.isSuccess()) {
            messageLabel.setForeground(UiTheme.MINT);
            messageLabel.setText(result.getMessage());
            refresh();
        } else {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText(result.getMessage());
        }
    }
}
