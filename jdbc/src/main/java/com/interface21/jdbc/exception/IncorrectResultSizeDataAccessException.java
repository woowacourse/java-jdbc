package com.interface21.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }

    public IncorrectResultSizeDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectResultSizeDataAccessException(Throwable cause) {
        super(cause);
    }
}
