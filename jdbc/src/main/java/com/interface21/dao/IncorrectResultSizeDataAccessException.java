package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends Throwable {

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual size: " + actualSize);
    }
}
