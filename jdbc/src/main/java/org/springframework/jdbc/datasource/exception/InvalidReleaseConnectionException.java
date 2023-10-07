package org.springframework.jdbc.datasource.exception;

public class InvalidReleaseConnectionException extends RuntimeException {

    public InvalidReleaseConnectionException(final String message) {
        super(message);
    }
}
