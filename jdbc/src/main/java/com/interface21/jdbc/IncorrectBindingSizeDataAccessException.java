package com.interface21.jdbc;

public class IncorrectBindingSizeDataAccessException extends RuntimeException {

    public IncorrectBindingSizeDataAccessException(int expectedSize, int actualSize) {
        super(String.format("Incorrect binding size: expected %s, actual %s", expectedSize, actualSize));
    }

    public IncorrectBindingSizeDataAccessException(String message, Exception e) {
        super(message, e);
    }
}
