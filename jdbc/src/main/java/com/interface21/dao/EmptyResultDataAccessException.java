package com.interface21.dao;

public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException() {
        super();
    }

    public EmptyResultDataAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public EmptyResultDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyResultDataAccessException(String message) {
        super(message);
    }

    public EmptyResultDataAccessException(Throwable cause) {
        super(cause);
    }
}
