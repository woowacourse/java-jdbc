package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;

public class IncorrectBindingSizeDataAccessException extends DataAccessException {

    public IncorrectBindingSizeDataAccessException(int expectedSize, int actualSize) {
        super(String.format("Incorrect binding size: expected %s, actual %s", expectedSize, actualSize));
    }

    public IncorrectBindingSizeDataAccessException(String message, Exception e) {
        super(message, e);
    }
}
