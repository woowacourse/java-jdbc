package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super(String.format("Incorrect result size: expected %s, actual %s", expectedSize, actualSize));
    }

    public IncorrectResultSizeDataAccessException(String message, Exception e) {
        super(message, e);
    }
}
