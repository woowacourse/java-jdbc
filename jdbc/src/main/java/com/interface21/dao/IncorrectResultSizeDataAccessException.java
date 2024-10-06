package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException() {
        super();
    }

    public IncorrectResultSizeDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }

    public IncorrectResultSizeDataAccessException(Throwable cause) {
        super(cause);
    }
}
