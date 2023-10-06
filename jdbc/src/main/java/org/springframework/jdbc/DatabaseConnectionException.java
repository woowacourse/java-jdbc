package org.springframework.jdbc;

public class DatabaseConnectionException extends RuntimeException {

    public DatabaseConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
