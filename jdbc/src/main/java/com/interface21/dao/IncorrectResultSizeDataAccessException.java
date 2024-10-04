package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(long expected, long actual) {
        super(String.format("Incorrect Result Size: Expected %d but actual %d", expected, actual));
    }
}
