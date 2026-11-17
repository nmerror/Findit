package com.bubt.findit.tools;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.config.AppConfig;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.db.Database;
import com.bubt.findit.db.DaoFactory;
import com.bubt.findit.ui.MainFrame;
import com.bubt.findit.ui.auth.LoginFrame;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Takes a screenshot of the application - handy for the project report and
 * for the presentation.
 *
 * <pre>
 *   java -cp target/classes com.bubt.findit.tools.Screenshot login      login.png
 *   java -cp target/classes com.bubt.findit.tools.Screenshot dashboard  dashboard.png
 *   java -cp target/classes com.bubt.findit.tools.Screenshot demo       dashboard-admin.png
 *   java -cp target/classes com.bubt.findit.tools.Screenshot profile    profile.png
 *   java -cp target/classes com.bubt.findit.tools.Screenshot users      users.png
 *   java -cp target/classes com.bubt.findit.tools.Screenshot audit      audit.png
 *   java -cp target/classes com.bubt.findit.tools.Screenshot segment2   segment2.png
 * </pre>
 *
 * <p>It always runs against the in-memory database, so it never touches your
 * real data.
 */
public final class Screenshot {

    private Screenshot() {
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "login";
        Path output = Path.of(args.length > 1 ? args[1] : "findit-screenshot.png");

        // "light:dashboard" takes the screenshot in the light palette.
        final String screen;
        if (mode.startsWith("light:")) {
            screen = mode.substring("light:".length());
            com.bubt.findit.ui.common.UiTheme.setMode(
                    com.bubt.findit.ui.common.UiTheme.Mode.LIGHT);
        } else {
            screen = mode;
            com.bubt.findit.ui.common.UiTheme.setMode(
                    com.bubt.findit.ui.common.UiTheme.Mode.DARK);
        }
        com.bubt.findit.ui.common.UiTheme.apply();

        AppConfig config = AppConfig.memory();
        Database.initialize(config);
        DaoFactory.reset();
        AppServices services = new AppServices(config);

        SwingUtilities.invokeAndWait(() -> {
            switch (screen) {
                case "demo", "users", "audit", "reports" -> {
                    DemoSegmentData.install();
                    seedAdmin(services);
                    services.authService().login("admin@findit.edu", "Admin@1234");
                    MainFrame frame = new MainFrame(services);
                    frame.setVisible(true);
                    if ("reports".equals(screen)) {
                        frame.navigateTo("reports");
                    } else if (!"demo".equals(screen)) {
                        frame.navigateTo(screen);
                    }
                }
                case "dashboard", "profile", "segment2", "search" -> {
                    services.authService().register("Ayesha Rahman", "ayesha@buet.edu", "21-04234",
                            "Passw0rd1", "Passw0rd1");
                    services.authService().login("ayesha@buet.edu", "Passw0rd1");
                    MainFrame frame = new MainFrame(services);
                    frame.setVisible(true);
                    if ("search".equals(screen)) {
                        frame.setSearchQuery("Ayesha");
                    } else if (!"dashboard".equals(screen)) {
                        frame.navigateTo("segment2".equals(screen) ? "s2-report" : screen);
                    }
                }
                default -> new LoginFrame(services).setVisible(true);
            }
        });

        Thread.sleep(1400);

        BufferedImage image = new Robot().createScreenCapture(
                new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        if (output.toAbsolutePath().getParent() != null) {
            Files.createDirectories(output.toAbsolutePath().getParent());
        }
        ImageIO.write(image, "png", output.toFile());
        System.out.println("saved " + output.toAbsolutePath());
        System.exit(0);
    }

    private static void seedAdmin(AppServices services) {
        services.authService().register("System Administrator", "admin@findit.edu", "ADMIN001",
                "Admin@1234", "Admin@1234");
        User admin = services.userDao().findByEmail("admin@findit.edu").orElse(null);
        if (admin != null) {
            services.userDao().updateRole(admin.getId(), Role.ADMIN);
        }
    }
}
