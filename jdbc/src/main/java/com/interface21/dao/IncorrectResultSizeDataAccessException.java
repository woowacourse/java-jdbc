package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException() {
        super();
    }

    public IncorrectResultSizeDataAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
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
