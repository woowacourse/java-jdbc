package org.springframework.jdbc.core;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }
}
