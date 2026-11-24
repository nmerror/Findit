package com.bubt.findit.db;

import com.bubt.findit.config.AppConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Owns the JDBC access to MySQL.
 *
 * <p>It holds the {@link ConnectionPool}, hands connections to the DAO layer and
 * offers a tiny helper for running several statements inside one transaction.
 *
 * <p>The MySQL driver class is referenced <b>by name</b> (not with a compile
 * time {@code import}) so that the project also compiles and runs on a machine
 * where the driver jar is not on the classpath yet - it only fails at runtime
 * with a clear message.
 */
public final class Database {

    private static volatile ConnectionPool pool;

    private Database() {
    }

    /** Prepares the database layer. Safe to call once at start-up. */
    public static synchronized void initialize(AppConfig appConfig) {
        if (pool != null) {
            return;
        }
        if (!appConfig.mysqlMode()) {
            return;
        }
        try {
            Class.forName(appConfig.dbDriver());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "MySQL JDBC driver not found: " + appConfig.dbDriver()
                            + ". Add mysql-connector-j to the classpath (see pom.xml).", ex);
        }
        pool = new ConnectionPool(appConfig.dbUrl(), appConfig.dbUser(), appConfig.dbPassword(),
                appConfig.dbPoolSize());
    }

    public static boolean isMysql() {
        return pool != null;
    }

    /**
     * Borrows a connection from the pool.
     *
     * <p>Always give it back with {@link #release(Connection)}:
     * <pre>
     * Connection connection = Database.getConnection();
     * try {
     *     ...
     * } finally {
     *     Database.release(connection);
     * }
     * </pre>
     */
    public static Connection getConnection() throws SQLException {
        ConnectionPool current = pool;
        if (current == null) {
            throw new SQLException("The database is running in memory mode - "
                    + "there is no JDBC connection available.");
        }
        return current.borrow();
    }

    /** Gives a connection back to the pool. */
    public static void release(Connection connection) {
        ConnectionPool current = pool;
        if (current != null) {
            current.release(connection);
        }
    }

    /**
     * Runs a block of work inside one transaction: everything is committed when
     * it finishes normally and rolled back when it throws.
     *
     * <pre>
     * Database.inTransaction(connection -&gt; {
     *     dao.insert(...);
     *     dao.update(...);
     *     return null;
     * });
     * </pre>
     */
    public static <T> T inTransaction(TransactionWork<T> work) throws SQLException {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (Exception ex) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                    // The original error is more interesting than the rollback error.
                }
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new SQLException("Transaction failed.", ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } finally {
            release(connection);
        }
    }

    /** True when the database answers. Used by the start-up self-check. */
    public static boolean ping() {
        ConnectionPool current = pool;
        if (current == null) {
            return false;
        }
        try (Connection connection = current.borrow()) {
            return connection.isValid(2);
        } catch (SQLException ex) {
            return false;
        }
    }

    /** Number of connections the pool currently holds. */
    public static int poolSize() {
        ConnectionPool current = pool;
        return current == null ? 0 : current.size();
    }

    /** Closes every pooled connection - call it when the app stops. */
    public static synchronized void shutdown() {
        if (pool != null) {
            pool.closeAll();
            pool = null;
        }
    }

    /** The work of a transaction. */
    @FunctionalInterface
    public interface TransactionWork<T> {
        T run(Connection connection) throws Exception;
    }
}
