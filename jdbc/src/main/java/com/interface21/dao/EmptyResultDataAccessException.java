package com.interface21.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    private static final int ACTUAL = 0;

    public EmptyResultDataAccessException(long expected) {
        super(expected, ACTUAL);
    }
}
