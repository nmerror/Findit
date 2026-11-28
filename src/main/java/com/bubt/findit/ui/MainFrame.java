package com.bubt.findit.ui;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.AuthContext;
import com.bubt.findit.core.model.User;
import com.bubt.findit.ui.auth.LoginFrame;
import com.bubt.findit.ui.common.AvatarLabel;
import com.bubt.findit.ui.common.BaseFrame;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.Icons;
import com.bubt.findit.ui.common.NavButton;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.TopBar;
import com.bubt.findit.ui.common.UiTheme;
import com.bubt.findit.ui.dashboard.DashboardPanel;
import com.bubt.findit.ui.posts.MyPostsPanel;
import com.bubt.findit.ui.posts.PostLostItemPanel;
import com.bubt.findit.ui.posts.PostManagementPanel;
import com.bubt.findit.ui.reports.ReportPanel;
import com.bubt.findit.ui.search.SearchResultsPanel;
import com.bubt.findit.ui.users.AuditLogPanel;
import com.bubt.findit.ui.users.ProfilePanel;
import com.bubt.findit.ui.users.UserManagementPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The main window: a navigation rail on the left, a header on top and the
 * screens of the segment inside the same frame.
 *
 * <p>Only the screens of Segment 1 are listed in the navigation. The screens of
 * Segment 2, 3 and 4 are added later by their owners with
 * {@link #addSegmentSection(String, String, String, JComponent)} - the window
 * already knows how to place them, so nothing has to be re-plumbed.
 */
public class MainFrame extends BaseFrame {

    private final AppServices services;
    private final User user;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentHolder = new JPanel(cardLayout);
    private final JPanel pageHolder = new JPanel(new BorderLayout());
    private final JPanel sidebarHost = new JPanel(new BorderLayout());

    private JPanel navigation;
    private TopBar topBar;
    private SearchResultsPanel searchResults;
    private NavButton activeButton;
    private String currentCard = "dashboard";
    private final Map<String, NavButton> navButtons = new HashMap<>();
    private final Map<String, String> cardTitles = new LinkedHashMap<>();

    public MainFrame(AppServices services) {
        super(services.config().appName(), "Campus Lost & Found Management System", 1240, 760);
        this.services = services;
        this.user = AuthContext.currentUser();

        buildShell();
        buildNavigation();
        buildContent();
    }

    // ------------------------------------------------------------- shell

    private void buildShell() {
        contentHolder.setBackground(UiTheme.WINDOW);
        pageHolder.setBackground(UiTheme.WINDOW);
        sidebarHost.setBackground(UiTheme.SIDEBAR);
        sidebarHost.setOpaque(false);

        navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.setOpaque(false);

        sidebarHost.removeAll();
        sidebarHost.add(buildSidebar(), BorderLayout.CENTER);
        sidebarHost.add(buildUserCard(), BorderLayout.SOUTH);

        searchResults = new SearchResultsPanel(services.searchService());
        topBar = new TopBar("Dashboard", "Welcome back, " + user.getFullName(), user,
                "search people, items or claims", this::runSearch);

        pageHolder.removeAll();
        pageHolder.add(topBar, BorderLayout.NORTH);
        pageHolder.add(contentHolder, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.WINDOW);
        root.add(sidebarHost, BorderLayout.WEST);
        root.add(pageHolder, BorderLayout.CENTER);

        getContent().removeAll();
        getContent().add(root, BorderLayout.CENTER);
        getContent().revalidate();
        getContent().repaint();
    }

    // ------------------------------------------------------------ sidebar

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UiTheme.STROKE);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(224, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 12, 14, 12));
        sidebar.add(navigation, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildUserCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_MD, UiTheme.SURFACE_2, UiTheme.STROKE_SOFT);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        AvatarLabel avatar = new AvatarLabel(user.initials(),
                user.isAdmin() ? UiTheme.VIOLET : UiTheme.ACCENT, 34);

        JLabel name = new JLabel(user.getFullName());
        name.setFont(Fonts.medium(12));
        name.setForeground(UiTheme.TEXT);

        JLabel role = new JLabel(user.getRole().label());
        role.setFont(Fonts.caption(11));
        role.setForeground(UiTheme.TEXT_MUTE);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        name.setAlignmentX(LEFT_ALIGNMENT);
        role.setAlignmentX(LEFT_ALIGNMENT);
        texts.add(name);
        texts.add(role);

        card.add(avatar, BorderLayout.WEST);
        card.add(texts, BorderLayout.CENTER);
        return card;
    }

    // ---------------------------------------------------------- navigation

    private void buildNavigation() {
        navigation.removeAll();

        addBrand();
        addNavButton("Dashboard", Icons.Glyph.DASHBOARD, "dashboard");

        if (!user.isAdmin()) {
            addSection("Lost and found");
            addNavButton("Post a Lost Item", Icons.Glyph.PLUS, "post");
            addNavButton("My Posts", Icons.Glyph.TAG, "my-posts");
        }

        addSection("Account");
        addNavButton("My Profile", Icons.Glyph.KEY, "profile");

        if (user.isAdmin()) {
            addSection("Office");
            addNavButton("Manage Accounts", Icons.Glyph.USERS, "users");
            addNavButton("Manage Posts", Icons.Glyph.BOX, "manage-posts");
            addNavButton("Audit Log", Icons.Glyph.LIST, "audit");
        }

        addSection("Insight");
        addNavButton("Search Results", Icons.Glyph.SEARCH, "search");
        addNavButton("Reports", Icons.Glyph.CHART, "reports");

        navigation.add(Box.createVerticalGlue());

        NavButton logout = new NavButton("Sign out", Icons.Glyph.LOGOUT);
        logout.setAlignmentX(0.5f);
        logout.addActionListener(event -> logout());
        navigation.add(logout);

        navigation.revalidate();
        navigation.repaint();
    }

    private void addBrand() {
        JLabel brand = new JLabel(services.config().appName());
        brand.setFont(Fonts.display(20));
        brand.setForeground(UiTheme.TEXT);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(2, 10, 14, 10));
        navigation.add(brand);
    }

    private void addSection(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(Fonts.medium(9));
        label.setForeground(UiTheme.TEXT_MUTE);
        label.setAlignmentX(0.5f);
        label.setBorder(BorderFactory.createEmptyBorder(14, 12, 6, 12));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        navigation.add(label);
    }

    private void addNavButton(String text, Icons.Glyph glyph, String cardName) {
        NavButton button = new NavButton(text, glyph);
        button.setAlignmentX(0.5f);
        button.addActionListener(event -> show(cardName, button));
        navButtons.put(cardName, button);
        navigation.add(button);
        if (cardName.equals("dashboard")) {
            activeButton = button;
        }
    }

    // ------------------------------------------------------------- content

    private void buildContent() {
        contentHolder.removeAll();

        contentHolder.add(new DashboardPanel(services, services.dashboardService(), user), "dashboard");
        contentHolder.add(new ProfilePanel(services, user), "profile");
        contentHolder.add(searchResults, "search");
        contentHolder.add(new ReportPanel(services.dashboardService(), services.reportService(),
                services.auditDao(), user), "reports");

        if (!user.isAdmin()) {
            contentHolder.add(new PostLostItemPanel(services, user), "post");
            contentHolder.add(new MyPostsPanel(services, user), "my-posts");
        }

        if (user.isAdmin()) {
            contentHolder.add(new UserManagementPanel(services, user), "users");
            contentHolder.add(new PostManagementPanel(services, user), "manage-posts");
            contentHolder.add(new AuditLogPanel(services, user), "audit");
        }

        cardTitles.put("dashboard", "Dashboard");
        cardTitles.put("profile", "My Profile");
        cardTitles.put("post", "Post a Lost Item");
        cardTitles.put("my-posts", "My Posts");
        cardTitles.put("users", "Manage Accounts");
        cardTitles.put("manage-posts", "Manage Posts");
        cardTitles.put("audit", "Audit Log");
        cardTitles.put("search", "Search Results");
        cardTitles.put("reports", "Reports");

        show(currentCard, navButtons.get(currentCard));
    }

    /**
     * Adds a screen of another segment to the window.
     *
     * <p>This is the seam for the other segments of the project: the owner of
     * Segment 2, 3 or 4 builds his panel and hands it over here. The window
     * creates the entry in the navigation, registers the panel and keeps track
     * of the title of the screen.
     *
     * <pre>
     *   mainFrame.addSegmentSection("Segment 2 - Reporting", "Report an Item",
     *           "s2-report", new ReportItemPanel(services));
     * </pre>
     *
     * @param section  the heading of the group in the navigation
     * @param item     the label of the entry in the navigation
     * @param cardName the internal name of the screen
     * @param panel    the screen itself
     */
    public void addSegmentSection(String section, String item, String cardName, JComponent panel) {
        addSection(section);
        addNavButton(item, Icons.Glyph.BOX, cardName);
        contentHolder.add(panel, cardName);
        cardTitles.put(cardName, item);
        navigation.revalidate();
        navigation.repaint();
        contentHolder.revalidate();
        contentHolder.repaint();
    }

    private void show(String cardName, NavButton button) {
        currentCard = cardName;
        cardLayout.show(contentHolder, cardName);
        if (activeButton != null && activeButton != button) {
            activeButton.setActiveState(false);
        }
        activeButton = button;
        if (button != null) {
            button.setActiveState(true);
        }
        if ("dashboard".equals(cardName)) {
            java.awt.Component component = contentHolder.getComponent(0);
            if (component instanceof DashboardPanel dashboard) {
                dashboard.refresh();
            }
        }
        updateTopBarTitle(cardName);
    }

    private void updateTopBarTitle(String cardName) {
        String title = cardTitles.getOrDefault(cardName, "Dashboard");
        if (topBar != null) {
            topBar.setTitle(title);
        }
    }

    /** Runs the global search and opens the results screen. */
    private void runSearch(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        searchResults.setQuery(text);
        show("search", navButtons.get("search"));
    }

    /** Runs a search and opens the results - used by the screenshot tool. */
    public void setSearchQuery(String text) {
        runSearch(text);
    }

    /** Jumps straight to a screen - used by the screenshot tool and by tests. */
    public void navigateTo(String cardName) {
        NavButton button = navButtons.get(cardName);
        if (button != null) {
            show(cardName, button);
        }
    }

    private void logout() {
        services.authService().logout();
        dispose();
        new LoginFrame(services).setVisible(true);
    }
}
