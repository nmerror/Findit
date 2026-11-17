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
import com.bubt.findit.ui.common.PlaceholderPanel;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.TopBar;
import com.bubt.findit.ui.common.UiTheme;
import com.bubt.findit.ui.dashboard.DashboardPanel;
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
import java.util.Map;

/**
 * The main window: a slim navigation rail on the left, a header on top and
 * the screens of every segment inside the same frame.
 *
 * <p>Every part of the window is rebuilt when the palette changes, so the dark
 * and the light mode are pixel-perfect and not just "good enough".
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

    public MainFrame(AppServices services) {
        super(services.config().appName(), "Campus Lost & Found Management System", 1280, 780);
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

    @Override
    protected void onThemePersisted() {
        services.config().saveTheme(UiTheme.mode().name().toLowerCase());
    }

    /** Rebuilds every screen with the new palette. */
    @Override
    protected void onThemeChanged() {
        navButtons.clear();
        activeButton = null;
        buildShell();
        buildNavigation();
        buildContent();
        show(currentCard, navButtons.get(currentCard));
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
                g2.setColor(UiTheme.STROKE_SOFT);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(248, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 14, 16, 14));
        sidebar.add(navigation, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildUserCard() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        AvatarLabel avatar = new AvatarLabel(user.initials(),
                user.isAdmin() ? UiTheme.VIOLET : UiTheme.ACCENT, 36);

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

        addSection("Account");
        addNavButton("My Profile", Icons.Glyph.KEY, "profile");
        if (user.isAdmin()) {
            addNavButton("Manage Users", Icons.Glyph.USERS, "users");
            addNavButton("Audit Log", Icons.Glyph.LIST, "audit");
        }

        addSection("Insight");
        addNavButton("Search Results", Icons.Glyph.SEARCH, "search");
        addNavButton("Reports", Icons.Glyph.CHART, "reports");

        addSection("Segment 2 — Reporting");
        addNavButton("Report Item", Icons.Glyph.PLUS, "s2-report");
        addNavButton("Browse Items", Icons.Glyph.BOX, "s2-browse");

        addSection("Segment 3 — Matching & Claims");
        addNavButton("My Claims", Icons.Glyph.TAG, "s3-claims");
        if (user.isAdmin()) {
            addNavButton("Match Results", Icons.Glyph.CHART, "s3-matches");
        }

        addSection("Segment 4 — Verification");
        if (user.isAdmin()) {
            addNavButton("Verify Claims", Icons.Glyph.SHIELD, "s4-verify");
            addNavButton("Returns", Icons.Glyph.CHECK, "s4-return");
        }

        navigation.add(Box.createVerticalGlue());

        NavButton logout = new NavButton("Sign out", Icons.Glyph.LOGOUT);
        logout.setAlignmentX(0.5f);
        logout.addActionListener(event -> logout());
        navigation.add(logout);

        navigation.revalidate();
        navigation.repaint();
    }

    private void addBrand() {
        JLabel brand = new JLabel("FindIt");
        brand.setFont(Fonts.display(22));
        brand.setForeground(UiTheme.TEXT);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(2, 10, 16, 10));
        navigation.add(brand);
    }

    private void addSection(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(Fonts.medium(9));
        label.setForeground(UiTheme.TEXT_MUTE);
        label.setAlignmentX(0.5f);
        label.setBorder(BorderFactory.createEmptyBorder(16, 12, 7, 12));
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

        contentHolder.add(new DashboardPanel(services.dashboardService(), user), "dashboard");
        contentHolder.add(new ProfilePanel(services, user), "profile");
        contentHolder.add(searchResults, "search");
        contentHolder.add(new ReportPanel(services.dashboardService(), services.reportService(),
                services.auditDao(), user), "reports");

        if (user.isAdmin()) {
            contentHolder.add(new UserManagementPanel(services, user), "users");
            contentHolder.add(new AuditLogPanel(services, user), "audit");
        }

        contentHolder.add(new PlaceholderPanel("SEGMENT 2", "Report a Lost or Found Item",
                "This screen belongs to Segment 2 — Lost & Found Item Reporting, Image Upload & "
                        + "Location Management.\n\nIt is already wired into the navigation and "
                        + "sits inside this window, so nothing has to be re-plumbed when the real "
                        + "screen arrives: replace this placeholder with your panel.",
                "Contract:  core.spi.ItemCatalogService   ->   ServiceRegistry.register(ItemCatalogService.class, yourImpl)"),
                "s2-report");

        contentHolder.add(new PlaceholderPanel("SEGMENT 2", "Browse Items",
                "This screen belongs to Segment 2. It lists every open report and uses the same "
                        + "ItemCatalogService contract that the dashboard already reads from.",
                "Contract:  core.spi.ItemCatalogService   ->   ServiceRegistry.register(ItemCatalogService.class, yourImpl)"),
                "s2-browse");

        contentHolder.add(new PlaceholderPanel("SEGMENT 3", "My Claims",
                "This screen belongs to Segment 3 — Intelligent Item Matching, Similarity Detection "
                        + "& Ownership Claim Management.\n\nRead the signed-in student from "
                        + "AuthContext.currentUser() — the same call this project already uses "
                        + "everywhere.",
                "Contract:  core.spi.ClaimQueryService + core.spi.MatchQueryService   ->   ServiceRegistry.register(...)"),
                "s3-claims");

        contentHolder.add(new PlaceholderPanel("SEGMENT 3", "Match Results",
                "This screen belongs to Segment 3. It shows the lost/found pairs produced by the "
                        + "matching engine together with their similarity score.",
                "Contract:  core.spi.MatchQueryService   ->   ServiceRegistry.register(MatchQueryService.class, yourImpl)"),
                "s3-matches");

        contentHolder.add(new PlaceholderPanel("SEGMENT 4", "Verify Claims",
                "This screen belongs to Segment 4 — Administrative Verification, Claim Approval & "
                        + "Item Return Management.\n\nIt is only reachable by administrators: the "
                        + "navigation already hides it from students, and the service layer checks "
                        + "the permission again.",
                "Contract:  core.spi.AdminWorkloadService   ->   ServiceRegistry.register(AdminWorkloadService.class, yourImpl)"),
                "s4-verify");

        contentHolder.add(new PlaceholderPanel("SEGMENT 4", "Item Return Management",
                "This screen belongs to Segment 4. It confirms that a returned item really reached "
                        + "its owner and closes the case.",
                "Contract:  core.spi.AdminWorkloadService   ->   ServiceRegistry.register(AdminWorkloadService.class, yourImpl)"),
                "s4-return");

        show(currentCard, navButtons.get(currentCard));
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
        String title = switch (cardName) {
            case "profile" -> "My Profile";
            case "users" -> "Manage Users";
            case "audit" -> "Audit Log";
            case "search" -> "Search Results";
            case "reports" -> "Reports";
            case "s2-report" -> "Report an Item";
            case "s2-browse" -> "Browse Items";
            case "s3-claims" -> "My Claims";
            case "s3-matches" -> "Match Results";
            case "s4-verify" -> "Verify Claims";
            case "s4-return" -> "Item Returns";
            default -> "Dashboard";
        };
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
