package com.bubt.findit.service;

/** Thrown when a logged-in user tries to do something their role does not allow. */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }
}
