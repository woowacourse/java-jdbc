package com.interface21.jdbc;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super(String.format("Incorrect result size: expected %s, actual %s", expectedSize, actualSize));
    }

    public IncorrectResultSizeDataAccessException(String message, Exception e) {
        super(message, e);
    }
}
