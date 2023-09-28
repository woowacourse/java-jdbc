package org.springframework.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException() {
        super("Incorrect result size!");
    }
}
