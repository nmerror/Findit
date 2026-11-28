package com.bubt.findit.core.event;

/** Names of the events Segment 1 publishes. Keep them stable. */
public final class Events {

    /** Wildcard - every event. */
    public static final String ALL = "*";

    public static final String USER_REGISTERED = "user.registered";
    public static final String USER_LOGIN_SUCCESS = "user.login.success";
    public static final String USER_LOGIN_FAILED = "user.login.failed";
    public static final String USER_LOGOUT = "user.logout";
    public static final String USER_STATUS_CHANGED = "user.status.changed";
    public static final String USER_ROLE_CHANGED = "user.role.changed";
    public static final String USER_DELETED = "user.deleted";
    public static final String PASSWORD_CHANGED = "user.password.changed";

    private Events() {
    }
}
