package com.interface21.jdbc.core;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    public EmptyResultDataAccessException(int expectedSize) {
        super(expectedSize, 0);
    }
}
