package com.interface21.dao;

public class IncorrectResultSizeException extends RuntimeException {

    public IncorrectResultSizeException(int expectedSize, int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", but got " + actualSize);
    }
}
