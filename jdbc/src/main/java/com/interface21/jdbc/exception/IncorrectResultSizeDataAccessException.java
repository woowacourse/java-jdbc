package com.interface21.jdbc.exception;

import com.interface21.dao.DataAccessException;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(int size) {
        super(String.format("Expected size 1 but %d", size));
    }
}
