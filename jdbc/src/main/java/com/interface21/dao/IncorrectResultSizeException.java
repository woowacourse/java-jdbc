package com.interface21.dao;

public class IncorrectResultSizeException extends RuntimeException {

    public IncorrectResultSizeException(long expected, long actual) {
        super("Expected: " + expected + ", but actual: " + actual);
    }
}
