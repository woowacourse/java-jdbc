package org.springframework.jdbc;

public class NotSingleResultException extends RuntimeException{

    public NotSingleResultException(final String message) {
        super(message);
    }
}
