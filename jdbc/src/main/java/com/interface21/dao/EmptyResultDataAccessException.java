package com.interface21.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    private static final int EXPECTED = 0;

    public EmptyResultDataAccessException(long size) {
        super(EXPECTED, size);
    }
}
