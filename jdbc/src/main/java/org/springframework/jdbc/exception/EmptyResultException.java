package org.springframework.jdbc.exception;

public class EmptyResultException extends RuntimeException {

    public EmptyResultException(String message) {
        super(message);
    }
}
