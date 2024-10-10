package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

public class DataSizeMismatchException extends DataAccessException {

    public DataSizeMismatchException(long expected, long actual) {
        super("Expected query result size: " + expected + ", fetched " + actual + " rows");
    }
}
