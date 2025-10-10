package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
    }
}
