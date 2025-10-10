package com.interface21.dao;

public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException(final int expectedSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual 0");
    }
}
