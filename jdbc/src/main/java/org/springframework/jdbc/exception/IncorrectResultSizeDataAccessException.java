package org.springframework.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException() {
    }

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }
}
