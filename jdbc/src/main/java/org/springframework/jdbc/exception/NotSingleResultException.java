package org.springframework.jdbc.exception;

public class NotSingleResultException extends RuntimeException {

    public NotSingleResultException(String message) {
        super(message);
    }
}
