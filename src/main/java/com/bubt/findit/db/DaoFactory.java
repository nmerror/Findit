package com.bubt.findit.db;

import com.bubt.findit.db.dao.AuditDao;
import com.bubt.findit.db.dao.ClaimDao;
import com.bubt.findit.db.dao.InMemoryAuditDao;
import com.bubt.findit.db.dao.InMemoryNotificationDao;
import com.bubt.findit.db.dao.InMemoryUserDao;
import com.bubt.findit.db.dao.ItemDao;
import com.bubt.findit.db.dao.JdbcAuditDao;
import com.bubt.findit.db.dao.JdbcClaimDao;
import com.bubt.findit.db.dao.JdbcItemDao;
import com.bubt.findit.db.dao.JdbcMatchDao;
import com.bubt.findit.db.dao.JdbcNotificationDao;
import com.bubt.findit.db.dao.JdbcUserDao;
import com.bubt.findit.db.dao.MatchDao;
import com.bubt.findit.db.dao.NotificationDao;
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
    private static NotificationDao notificationDao;
    private static ItemDao itemDao;
    private static ClaimDao claimDao;
    private static MatchDao matchDao;

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

    public static NotificationDao notificationDao() {
        if (notificationDao == null) {
            notificationDao = Database.isMysql() ? new JdbcNotificationDao() : new InMemoryNotificationDao();
        }
        return notificationDao;
    }

    public static ItemDao itemDao() {
        if (itemDao == null) {
            itemDao = Database.isMysql() ? new JdbcItemDao() : null;
        }
        return itemDao;
    }

    public static ClaimDao claimDao() {
        if (claimDao == null) {
            claimDao = Database.isMysql() ? new JdbcClaimDao() : null;
        }
        return claimDao;
    }

    public static MatchDao matchDao() {
        if (matchDao == null) {
            matchDao = Database.isMysql() ? new JdbcMatchDao() : null;
        }
        return matchDao;
    }

    public static void reset() {
        userDao = null;
        auditDao = null;
        notificationDao = null;
        itemDao = null;
        claimDao = null;
        matchDao = null;
    }
}
