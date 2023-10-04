package org.springframework.jdbc.exception;

public class NotSingleResultException extends RuntimeException {

    public NotSingleResultException(final String message) {
        super(message);
    }
}
