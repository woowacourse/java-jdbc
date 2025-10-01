package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException{

    public IncorrectResultSizeDataAccessException(final String message) {
        super(message);
    }

    public IncorrectResultSizeDataAccessException(final Throwable cause) {
        super(cause);
    }

    public IncorrectResultSizeDataAccessException() {
        super();
    }
}
