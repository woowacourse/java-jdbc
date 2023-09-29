package org.springframework.dao;

public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException() {
        super("Incorrect result size: expected " + 1 + ", actual " + 0);
    }
}
