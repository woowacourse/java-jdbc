package org.springframework.jdbc.exception;

import org.springframework.dao.DataAccessException;

public class IncorrectResultSizeDataAccessException extends DataAccessException {
    public IncorrectResultSizeDataAccessException(int expected, int actual) {
        super("Incorrect result size: expected " + expected + ", actual " + actual);
    }
}
