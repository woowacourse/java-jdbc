package org.springframework.jdbc.core.exception;

public class ResultSetOverflowException extends IllegalArgumentException {

    public ResultSetOverflowException(final String message) {
        super(message);
    }
}
