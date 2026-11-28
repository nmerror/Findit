package com.bubt.findit.ui.dashboard;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.User;
import com.bubt.findit.service.DashboardService;
import com.bubt.findit.service.PostService;
import com.bubt.findit.ui.common.EmptyState;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.Icons;
import com.bubt.findit.ui.common.ModernTable;
import com.bubt.findit.ui.common.PhotoRenderer;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.StatCard;
import com.bubt.findit.ui.common.StatusTableRenderer;
import com.bubt.findit.ui.common.StretchPanel;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The dashboard - the screen the user sees after the login.
 *
 * <p>It shows three things and nothing more: how many lost items are on the
 * board, the lost items themselves and the accounts that exist. The numbers
 * come from the database, so they are zero until somebody really posts.
 */
public class DashboardPanel extends JPanel {

    /** All rows of the dashboard share this alignment, so they all get the
     *  full width of the window. */
    private static final float ROW_ALIGNMENT = 0.5f;

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final AppServices services;
    private final DashboardService dashboardService;
    private final PostService postService;
    private final User viewer;

    private final StatCard lostCard = new StatCard("Lost posts", 0, "waiting for their owner",
            Icons.Glyph.BOX, UiTheme.ACCENT);
    private final StatCard foundCard = new StatCard("Found posts", 0, "waiting to be claimed",
            Icons.Glyph.CHECK, UiTheme.MINT);
    private final StatCard accountCard = new StatCard("Accounts", 0, "registered users",
            Icons.Glyph.USERS, UiTheme.VIOLET);
    private final StatCard ownCard = new StatCard("My posts", 0, "posted by you",
            Icons.Glyph.TAG, UiTheme.AMBER);

    private final DefaultTableModel postModel = new DefaultTableModel(
            new Object[]{"Photo", "Item", "Category", "Where", "Posted by", "Posted on"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel accountModel = new DefaultTableModel(
            new Object[]{"Name", "E-mail", "Student ID", "Role", "Joined"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final CardLayout postCards = new CardLayout();
    private final JPanel postArea = new JPanel(postCards);
    private final JTable postTable = ModernTable.style(new JTable(postModel));
    private final JTable accountTable = ModernTable.style(new JTable(accountModel));

    public DashboardPanel(AppServices services, DashboardService dashboardService, User viewer) {
        super(new BorderLayout());
        this.services = services;
        this.dashboardService = dashboardService;
        this.postService = services.postService();
        this.viewer = viewer;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        add(buildBody(), BorderLayout.CENTER);
        refresh();
    }

    // --------------------------------------------------------------- layout

    private javax.swing.JComponent buildBody() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(lostCard);
        cards.add(foundCard);
        cards.add(accountCard);
        cards.add(ownCard);

        StretchPanel body = StretchPanel.vertical();
        body.setOpaque(false);

        cards.setAlignmentX(ROW_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        cards.setMinimumSize(new Dimension(520, 132));
        body.add(cards);
        body.add(javax.swing.Box.createVerticalStrut(14));

        body.add(buildPostsCard());
        body.add(javax.swing.Box.createVerticalStrut(14));

        body.add(buildAccountsCard());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel buildPostsCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(sectionTitle("Recent lost posts"), BorderLayout.NORTH);

        postTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {64, 185, 155, 160, 150, 150};
        for (int i = 0; i < widths.length; i++) {
            postTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        postTable.getColumnModel().getColumn(0).setCellRenderer(new PhotoRenderer());

        postArea.setOpaque(false);
        postArea.add(new EmptyState("No lost item has been posted yet",
                "The board is empty - the first post will appear here."), "empty");
        postArea.add(new JScrollPane(postTable), "table");

        card.add(postArea, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAccountsCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(sectionTitle(viewer != null && viewer.isAdmin()
                ? "All accounts" : "Registered students"), BorderLayout.NORTH);

        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {190, 220, 120, 110, 150};
        for (int i = 0; i < widths.length; i++) {
            accountTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        accountTable.getColumnModel().getColumn(3).setCellRenderer(new StatusTableRenderer());

        JScrollPane scroll = ModernTable.scroll(accountTable);
        scroll.setPreferredSize(new Dimension(600, 200));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(Fonts.medium(11));
        label.setForeground(UiTheme.TEXT_MUTE);
        return label;
    }

    // -------------------------------------------------------------- refresh

    /** Reads the numbers and the rows from the database again. */
    public void refresh() {
        var stats = dashboardService.build(viewer);

        lostCard.setValue(stats.getTotalLostItems());
        lostCard.setDetail("waiting for their owner");
        foundCard.setValue(stats.getTotalFoundItems());
        foundCard.setDetail("nothing found yet");

        List<User> accounts = services.userService().visibleAccounts(viewer, 100);
        accountCard.setValue(accounts.size());
        accountCard.setDetail(viewer != null && viewer.isAdmin()
                ? "students and managers" : "registered students");

        List<ItemSummary> own = postService.myPosts(viewer == null ? 0 : viewer.getId(), 100);
        ownCard.setValue(own.size());
        ownCard.setDetail(own.isEmpty() ? "nothing posted yet" : "posted by you");

        loadPosts();
        loadAccounts(accounts);
    }

    private void loadPosts() {
        List<ItemSummary> posts = postService.openLostPosts(50);
        postModel.setRowCount(0);
        for (ItemSummary post : posts) {
            postModel.addRow(new Object[]{
                    post.getImagePath(),
                    post.getTitle(),
                    com.bubt.findit.core.model.ItemCategory.fromString(post.getCategory()).label(),
                    post.getLocationLabel(),
                    post.getPostedByName(),
                    post.getCreatedAt() == null ? "" : post.getCreatedAt().format(DATE)});
        }
        postCards.show(postArea, posts.isEmpty() ? "empty" : "table");
    }

    private void loadAccounts(List<User> accounts) {
        accountModel.setRowCount(0);
        for (User user : accounts) {
            accountModel.addRow(new Object[]{
                    user.getFullName(),
                    user.getEmail(),
                    user.getStudentId(),
                    user.getRole().label(),
                    user.getCreatedAt() == null ? "" : user.getCreatedAt().format(DATE)});
        }
    }
}
