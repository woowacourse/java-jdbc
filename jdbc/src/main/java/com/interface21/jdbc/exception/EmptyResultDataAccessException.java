package com.interface21.jdbc.exception;

public class EmptyResultDataAccessException extends RuntimeException {

    public EmptyResultDataAccessException(String message) {
        super(message);
    }

    public EmptyResultDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyResultDataAccessException(Throwable cause) {
        super(cause);
    }
}
