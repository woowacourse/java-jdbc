package com.interface21.jdbc.exception;

import com.interface21.dao.DataAccessException;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super(String.format("Expected size %d but %d", expectedSize, actualSize));
    }
}
