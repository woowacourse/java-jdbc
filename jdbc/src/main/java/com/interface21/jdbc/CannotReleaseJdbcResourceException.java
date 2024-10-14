package com.interface21.jdbc;

public class CannotReleaseJdbcResourceException extends RuntimeException {
    public CannotReleaseJdbcResourceException(Throwable cause) {
        super(cause);
    }
}
