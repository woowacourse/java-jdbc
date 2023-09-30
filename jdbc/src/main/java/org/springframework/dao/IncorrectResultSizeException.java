package org.springframework.dao;

public class IncorrectResultSizeException extends RuntimeException {

    public IncorrectResultSizeException(final String message) {
        super(message);
    }
}
