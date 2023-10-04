package org.springframework.jdbc.core.exception;

public class DataSourceNotFoundException extends RuntimeException {
    public DataSourceNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
