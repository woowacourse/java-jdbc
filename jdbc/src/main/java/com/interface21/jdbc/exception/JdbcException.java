package com.interface21.jdbc.exception;

public class JdbcException extends RuntimeException {

    public JdbcException(String message) {
        super(message);
    }

    public JdbcException(final Throwable cause) {
        super(cause);
    }
}
