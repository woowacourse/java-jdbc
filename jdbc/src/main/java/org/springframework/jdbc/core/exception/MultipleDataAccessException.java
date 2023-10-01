package org.springframework.jdbc.core.exception;

public class MultipleDataAccessException extends RuntimeException {

    public MultipleDataAccessException(final String message) {
        super(message);
    }
}
