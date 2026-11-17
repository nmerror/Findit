package com.bubt.findit.db;

import com.bubt.findit.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Owns the JDBC connection to MySQL.
 *
 * <p>The MySQL driver class is referenced by <b>name</b> (not with a compile
 * time {@code import}) so that the project also compiles and runs on a machine
 * where the driver jar is not on the classpath yet - it only fails at runtime
 * with a clear message.
 */
public final class Database {

    private static AppConfig config;

    private Database() {
    }

    public static void initialize(AppConfig appConfig) {
        config = appConfig;
        if (appConfig.mysqlMode()) {
            try {
                Class.forName(appConfig.dbDriver());
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(
                        "MySQL JDBC driver not found: " + appConfig.dbDriver()
                                + ". Add mysql-connector-j to the classpath (see pom.xml).", ex);
            }
        }
    }

    public static boolean isMysql() {
        return config != null && config.mysqlMode();
    }

    /** A new connection. Always close it - use try-with-resources. */
    public static Connection getConnection() throws SQLException {
        if (!isMysql()) {
            throw new SQLException("Database is running in memory mode - no JDBC connection available.");
        }
        return DriverManager.getConnection(config.dbUrl(), config.dbUser(), config.dbPassword());
    }
}
