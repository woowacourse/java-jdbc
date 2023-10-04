package org.springframework.dao;

public class ResultEmptyException extends RuntimeException {

    public ResultEmptyException(final String message) {
        super(message);
    }
}
