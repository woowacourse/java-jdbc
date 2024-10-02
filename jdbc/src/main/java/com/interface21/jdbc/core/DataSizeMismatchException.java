package com.interface21.jdbc.core;

public class DataSizeMismatchException extends RuntimeException {

    public DataSizeMismatchException(long expected, long actual) {
        super("Expected query result size: " + expected + ", fetched " + actual + " rows");
    }
}
