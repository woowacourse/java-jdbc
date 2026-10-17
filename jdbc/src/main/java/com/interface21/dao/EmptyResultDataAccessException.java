package com.interface21.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    private static final long serialVersionUID = 1L;

    public EmptyResultDataAccessException(final int expectedSize) {
        super(expectedSize, 0);
    }
}
