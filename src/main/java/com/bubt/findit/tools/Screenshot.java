package com.bubt.findit.tools;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.config.AppConfig;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.db.Database;
import com.bubt.findit.db.DaoFactory;
import com.bubt.findit.ui.MainFrame;
import com.bubt.findit.ui.auth.ForgotPasswordFrame;
import com.bubt.findit.ui.auth.LoginFrame;
import com.bubt.findit.ui.auth.RegisterFrame;

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
 * <p>By default it runs against the in-memory database, so it never touches
 * your real data. Add {@code --mysql} as the third argument to take the
 * picture against the real MySQL database instead:
 *
 * <pre>
 *   java -cp "target/classes:lib/*" com.bubt.findit.tools.Screenshot demo out.png --mysql
 * </pre>
 */
public final class Screenshot {

    private Screenshot() {
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "login";
        Path output = Path.of(args.length > 1 ? args[1] : "findit-screenshot.png");

        final String screen = mode;
        com.bubt.findit.ui.common.UiTheme.apply();

        boolean useMysql = args.length > 2 && "--mysql".equals(args[2]);
        AppConfig config = useMysql ? AppConfig.load() : AppConfig.memory();
        Database.initialize(config);
        DaoFactory.reset();
        AppServices services = new AppServices(config);

        SwingUtilities.invokeAndWait(() -> {
            switch (screen) {
                case "register" -> new RegisterFrame(services).setVisible(true);
                case "reset" -> new ForgotPasswordFrame(services).setVisible(true);
                case "demo", "users", "posts", "audit", "reports" -> {
                    // The screenshots show the real database - no fake rows.
                    seedAdmin(services);
                    services.authService().login("admin@findit.edu", "Admin@1234");
                    MainFrame frame = new MainFrame(services);
                    frame.setVisible(true);
                    if ("reports".equals(screen)) {
                        frame.navigateTo("reports");
                    } else if ("posts".equals(screen)) {
                        frame.navigateTo("manage-posts");
                    } else if (!"demo".equals(screen)) {
                        frame.navigateTo(screen);
                    }
                }
                case "dashboard", "profile", "search", "post", "my-posts" -> {
                    // The student account of the seed data - it is created when
                    // it does not exist yet.
                    if (services.userDao().findByEmail("ayesha@buet.edu").isEmpty()) {
                        services.authService().register("Ayesha Rahman", "ayesha@buet.edu",
                                "21-04234", "Student@123", "Student@123");
                    }
                    services.authService().login("ayesha@buet.edu", "Student@123");
                    MainFrame frame = new MainFrame(services);
                    frame.setVisible(true);
                    if ("search".equals(screen)) {
                        frame.setSearchQuery("Ayesha");
                    } else if ("post".equals(screen)) {
                        frame.navigateTo("post");
                    } else if ("my-posts".equals(screen)) {
                        frame.navigateTo("my-posts");
                    } else if (!"dashboard".equals(screen)) {
                        frame.navigateTo(screen);
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
