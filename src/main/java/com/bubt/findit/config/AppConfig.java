package com.bubt.findit.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Application settings, read from {@code app.properties}.
 *
 * <p>Two modes:
 * <ul>
 *   <li>{@code app.mode=mysql} - the real thing (MySQL 8 + JDBC, as required
 *       by Section 9 of the proposal).</li>
 *   <li>{@code app.mode=memory} - everything runs in RAM. Useful while you are
 *       developing and for the automated self-test, because it needs no server.</li>
 * </ul>
 */
public class AppConfig {

    private final String appName;
    private final boolean mysqlMode;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String dbDriver;
    private final int sessionTimeoutMinutes;
    private final String theme;
    private final int dbPoolSize;

    public AppConfig(String appName, boolean mysqlMode, String dbUrl, String dbUser,
                     String dbPassword, String dbDriver, int sessionTimeoutMinutes) {
        this(appName, mysqlMode, dbUrl, dbUser, dbPassword, dbDriver, sessionTimeoutMinutes, "dark");
    }

    public AppConfig(String appName, boolean mysqlMode, String dbUrl, String dbUser,
                     String dbPassword, String dbDriver, int sessionTimeoutMinutes, String theme) {
        this(appName, mysqlMode, dbUrl, dbUser, dbPassword, dbDriver, sessionTimeoutMinutes, theme, 8);
    }

    public AppConfig(String appName, boolean mysqlMode, String dbUrl, String dbUser,
                     String dbPassword, String dbDriver, int sessionTimeoutMinutes, String theme,
                     int dbPoolSize) {
        this.appName = appName;
        this.mysqlMode = mysqlMode;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbDriver = dbDriver;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        this.theme = theme == null ? "dark" : theme;
        this.dbPoolSize = dbPoolSize < 1 ? 1 : dbPoolSize;
    }

    /** Reads {@code app.properties} from the classpath, then from the working directory. */
    public static AppConfig load() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ex) {
            System.err.println("[config] could not read app.properties from classpath: " + ex.getMessage());
        }
        Path external = Path.of("app.properties");
        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                props.load(in);
            } catch (IOException ex) {
                System.err.println("[config] could not read app.properties: " + ex.getMessage());
            }
        }
        return from(props);
    }

    public static AppConfig from(Properties props) {
        return new AppConfig(
                props.getProperty("app.name", "FindIt"),
                !"memory".equalsIgnoreCase(props.getProperty("app.mode", "mysql")),
                props.getProperty("db.url", "jdbc:mysql://localhost:3306/findit?useSSL=false&serverTimezone=UTC"),
                props.getProperty("db.user", "root"),
                props.getProperty("db.password", ""),
                props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"),
                Integer.parseInt(props.getProperty("session.timeout.minutes", "60")),
                props.getProperty("app.theme", "dark"),
                Integer.parseInt(props.getProperty("db.pool.size", "8")));
    }

    /** In-memory configuration - no MySQL server needed. */
    public static AppConfig memory() {
        return new AppConfig("FindIt", false, "", "", "", "", 60, "dark", 4);
    }

    public String appName() {
        return appName;
    }

    public boolean mysqlMode() {
        return mysqlMode;
    }

    public String dbUrl() {
        return dbUrl;
    }

    public String dbUser() {
        return dbUser;
    }

    public String dbPassword() {
        return dbPassword;
    }

    public String dbDriver() {
        return dbDriver;
    }

    public int sessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    /** "dark" or "light" - the palette the application starts in. */
    public String theme() {
        return theme;
    }

    /** How many JDBC connections the pool keeps open (see db.pool.size). */
    public int dbPoolSize() {
        return dbPoolSize;
    }

    /**
     * Remembers the chosen palette in app.properties. Silently does nothing
     * when the file cannot be written (for example on a read-only checkout).
     */
    public void saveTheme(String value) {
        Path file = Path.of("app.properties");
        try {
            if (Files.exists(file)) {
                String content = Files.readString(file);
                java.util.regex.Pattern pattern =
                        java.util.regex.Pattern.compile("(?m)^app\\.theme\\s*=.*$");
                if (pattern.matcher(content).find()) {
                    content = pattern.matcher(content).replaceFirst(
                            java.util.regex.Matcher.quoteReplacement("app.theme = " + value));
                } else {
                    content = content + System.lineSeparator() + "app.theme = " + value
                            + System.lineSeparator();
                }
                Files.writeString(file, content);
            }
        } catch (IOException | java.util.regex.PatternSyntaxException ignored) {
            // Not being able to remember the theme is not a failure.
        }
    }
}
