package org.springframework.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(int size) {
        super("Incorrect Result Size Data returned! returned data size was " + size);
    }
}
