package com.interface21.jdbc;

public class JdbcException extends RuntimeException {
    public JdbcException(final String message) {
        super(message);
    }

    public JdbcException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
