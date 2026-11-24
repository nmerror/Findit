package com.bubt.findit.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A small, dependency-free connection pool.
 *
 * <p>Why not {@code DriverManager} directly? Every call would open a brand new
 * TCP connection to MySQL and close it again - on a campus network that is slow
 * enough to be noticeable, and on a shared MySQL server it exhausts the
 * {@code max_connections} limit quickly. The pool keeps a small number of
 * connections open and hands them out on demand.
 *
 * <p>It is intentionally tiny and has no third-party dependency, so the project
 * still compiles and runs with a plain {@code javac} (no Maven, no jars).
 *
 * <p><b>How the DAO code uses it:</b> {@link Database#getConnection()} returns
 * a proxy around the real connection. Calling {@code close()} on it - which
 * {@code try-with-resources} does automatically - returns the connection to the
 * pool instead of closing it. That is why the existing DAO code did not have to
 * change.
 */
final class ConnectionPool {

    /** How long a caller waits for a free connection before giving up. */
    private static final long BORROW_TIMEOUT_SECONDS = 8;

    private final String url;
    private final String user;
    private final String password;
    private final int maxSize;

    /** Connections that are currently free to be handed out. */
    private final ArrayBlockingQueue<Connection> idle;

    /** Every real connection this pool has created, so all of them can be closed. */
    private final List<Connection> all = new ArrayList<>();

    /** How many connections exist in total (idle + borrowed). */
    private final AtomicInteger size = new AtomicInteger();

    private volatile boolean closed;

    ConnectionPool(String url, String user, String password, int maxSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxSize = Math.max(1, maxSize);
        this.idle = new ArrayBlockingQueue<>(this.maxSize);
    }

    /**
     * Takes a connection out of the pool.
     *
     * <p>The returned connection is a proxy: {@code close()} on it gives the
     * connection back to the pool.
     */
    Connection borrow() throws SQLException {
        if (closed) {
            throw new SQLException("The connection pool is closed.");
        }
        Connection real = idle.poll();
        if (real == null) {
            if (size.get() < maxSize && size.incrementAndGet() <= maxSize) {
                try {
                    real = DriverManager.getConnection(url, user, password);
                    all.add(real);
                } catch (SQLException ex) {
                    size.decrementAndGet();
                    throw ex;
                }
            } else {
                size.decrementAndGet();
                real = waitForIdle();
            }
        }
        if (real == null) {
            throw new SQLException("Timed out waiting for a free database connection (pool size "
                    + maxSize + ").");
        }
        if (!real.isValid(2)) {
            closeQuietly(real);
            size.decrementAndGet();
            return borrow();
        }
        real.setAutoCommit(true);
        return wrap(real);
    }

    /**
     * Gives a connection back to the pool.
     *
     * <p>Accepts both the proxy handed out by {@link #borrow()} and a raw
     * connection; calling it twice for the same connection is harmless.
     */
    void release(Connection connection) {
        if (connection == null) {
            return;
        }
        PooledHandler handler = handlerOf(connection);
        if (handler != null) {
            handler.release();
            return;
        }
        // A raw connection that was never wrapped - close it so it is not leaked.
        closeQuietly(connection);
        size.decrementAndGet();
    }

    /** Closes every connection of the pool. Called when the app shuts down. */
    void closeAll() {
        closed = true;
        for (Connection connection : all) {
            closeQuietly(connection);
        }
        all.clear();
        idle.clear();
        size.set(0);
    }

    int size() {
        return size.get();
    }

    /** Wraps the real connection in a proxy whose close() returns it to the pool. */
    private Connection wrap(Connection real) {
        PooledHandler handler = new PooledHandler(real, this);
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler);
    }

    /** Returns the pool handler of a pooled connection, or null for a raw one. */
    private static PooledHandler handlerOf(Connection connection) {
        if (!Proxy.isProxyClass(connection.getClass())) {
            return null;
        }
        InvocationHandler handler = Proxy.getInvocationHandler(connection);
        return handler instanceof PooledHandler ? (PooledHandler) handler : null;
    }

    private Connection waitForIdle() throws SQLException {
        try {
            return idle.poll(BORROW_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a database connection.", ex);
        }
    }

    private static void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Nothing useful can be done when closing fails.
        }
    }

    /**
     * Intercepts {@code close()} on the proxy and hands the connection back to
     * the pool instead of closing it.
     */
    private static final class PooledHandler implements InvocationHandler {

        private final Connection real;
        private final ConnectionPool pool;
        private boolean released;

        private PooledHandler(Connection real, ConnectionPool pool) {
            this.real = real;
            this.pool = pool;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("close".equals(name)) {
                release();
                return null;
            }
            if ("isClosed".equals(name)) {
                return released || real.isClosed();
            }
            if (released) {
                throw new SQLException("This connection has already been returned to the pool.");
            }
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }

        private void release() {
            if (released) {
                return;
            }
            released = true;
            try {
                if (!real.getAutoCommit()) {
                    real.rollback();
                    real.setAutoCommit(true);
                }
            } catch (SQLException ignored) {
                // A broken connection is simply dropped instead of being reused.
            }
            if (pool.closed || !pool.idle.offer(real)) {
                closeQuietly(real);
                pool.size.decrementAndGet();
            }
        }
    }
}
