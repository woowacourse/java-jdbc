package com.interface21.jdbc;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    public EmptyResultDataAccessException(int expectedSize) {
        super(0, expectedSize);
    }

    public EmptyResultDataAccessException(String message, Exception e) {
        super(message, e);
    }
}
