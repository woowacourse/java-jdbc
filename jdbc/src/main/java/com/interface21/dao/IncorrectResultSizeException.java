package com.interface21.dao;

public class IncorrectResultSizeException extends DataAccessException {

    public IncorrectResultSizeException(int expectedSize, int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", but actual " + actualSize);
    }
}
