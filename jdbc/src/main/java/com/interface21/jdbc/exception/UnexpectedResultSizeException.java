package com.interface21.jdbc.exception;

public class UnexpectedResultSizeException extends RuntimeException {

    public UnexpectedResultSizeException(String message) {
        super(message);
    }

    public UnexpectedResultSizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
