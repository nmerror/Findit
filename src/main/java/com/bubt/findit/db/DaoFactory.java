package com.bubt.findit.db;

import com.bubt.findit.db.dao.AuditDao;
import com.bubt.findit.db.dao.InMemoryAuditDao;
import com.bubt.findit.db.dao.InMemoryUserDao;
import com.bubt.findit.db.dao.JdbcAuditDao;
import com.bubt.findit.db.dao.JdbcUserDao;
import com.bubt.findit.db.dao.UserDao;

/**
 * Chooses the DAO implementation that matches the current mode.
 *
 * <p>This is the only place that knows whether the app runs on MySQL or in
 * memory - the rest of the code only sees the interfaces.
 */
public final class DaoFactory {

    private static UserDao userDao;
    private static AuditDao auditDao;

    private DaoFactory() {
    }

    public static UserDao userDao() {
        if (userDao == null) {
            userDao = Database.isMysql() ? new JdbcUserDao() : new InMemoryUserDao();
        }
        return userDao;
    }

    public static AuditDao auditDao() {
        if (auditDao == null) {
            auditDao = Database.isMysql() ? new JdbcAuditDao() : new InMemoryAuditDao();
        }
        return auditDao;
    }

    public static void reset() {
        userDao = null;
        auditDao = null;
    }
}
