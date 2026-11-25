package com.bubt.findit;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.config.AppConfig;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.db.Database;
import com.bubt.findit.db.DaoFactory;
import com.bubt.findit.ui.auth.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * Application entry point.
 *
 * <p>Start it with:
 * <pre>
 *   java -cp target/classes com.bubt.findit.Main          # uses app.properties
 *   java -cp target/classes com.bubt.findit.Main --memory # no MySQL needed
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        AppConfig config = resolveConfig(args);
        com.bubt.findit.ui.common.UiTheme.apply();
        Database.initialize(config);
        DaoFactory.reset();

        AppServices services = new AppServices(config);
        if (hasFlag(args, "--demo")) {
            // Fake data for Segments 2/3/4 so the dashboard can be presented
            // before they are finished.
            com.bubt.findit.tools.DemoSegmentData.install();
        }
        seedAdministrator(services);

        System.out.println("=========================================");
        System.out.println("  " + config.appName() + " - Campus Lost & Found Management");
        System.out.println("  Mode: " + (config.mysqlMode() ? "MySQL (JDBC)" : "In-memory demo"));
        System.out.println("=========================================");

        SwingUtilities.invokeLater(() -> new LoginFrame(services).setVisible(true));
    }

    private static AppConfig resolveConfig(String[] args) {
        for (String arg : args) {
            if ("--memory".equalsIgnoreCase(arg)) {
                return AppConfig.memory();
            }
        }
        return AppConfig.load();
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the first administrator if there is none yet, so that you can
     * log in to the admin console. The password is printed to the console.
     */
    private static void seedAdministrator(AppServices services) {
        boolean anyAdmin = services.userDao().countByRole(Role.ADMIN) > 0;
        if (anyAdmin) {
            return;
        }
        String email = "admin@findit.edu";
        if (services.userDao().existsByEmail(email)) {
            return;
        }
        OperationResult<User> result = services.authService().register(
                "System Administrator", email, "ADMIN-001", "Admin@1234", "Admin@1234");
        if (result.isSuccess()) {
            User admin = services.userDao().findByEmail(email).orElse(null);
            if (admin != null) {
                services.userDao().updateRole(admin.getId(), Role.ADMIN);
                services.userDao().updateStatus(admin.getId(),
                        com.bubt.findit.core.model.UserStatus.ACTIVE);
            }
            System.out.println("Created default administrator: " + email + " / Admin@1234");
        }
    }
}
