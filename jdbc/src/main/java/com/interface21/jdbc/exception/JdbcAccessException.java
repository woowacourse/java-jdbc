package com.interface21.jdbc.exception;

public class JdbcAccessException extends RuntimeException {

    public JdbcAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcAccessException(String message) {
        super(message);
    }
}
