package com.bubt.findit.db.dao;

/** Wraps a {@link java.sql.SQLException} so that the service layer stays clean. */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
