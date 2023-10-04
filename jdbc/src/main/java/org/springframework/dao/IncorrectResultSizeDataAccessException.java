package org.springframework.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }
}
