package com.bubt.findit.core.model;

/**
 * Small result wrapper used across the service layer.
 *
 * <p>Why not exceptions everywhere? Services that are called from Swing event
 * handlers need to show a friendly message instead of blowing up. Returning a
 * result keeps the service layer free of Swing code and keeps Segments 2/3/4
 * decoupled from how their screens report errors.
 */
public class OperationResult<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;

    private OperationResult(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    public static <T> OperationResult<T> ok(T data) {
        return new OperationResult<>(true, null, data, null);
    }

    public static <T> OperationResult<T> ok(T data, String message) {
        return new OperationResult<>(true, message, data, null);
    }

    public static <T> OperationResult<T> fail(String errorCode, String message) {
        return new OperationResult<>(false, message, null, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "OperationResult{success=" + success + ", message='" + message + "'}";
    }
}
