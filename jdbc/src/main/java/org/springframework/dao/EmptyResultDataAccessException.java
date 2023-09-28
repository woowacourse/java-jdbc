package org.springframework.dao;

public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException() {
        super("No data is available!");
    }
}
