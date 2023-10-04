package org.springframework.dao;

public class IncorrectResultSizeDataException extends DataAccessException{

    public IncorrectResultSizeDataException(final String message) {
        super(message);
    }
}
