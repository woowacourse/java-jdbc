package org.springframework.jdbc;

public class CannotCloseStatementException extends RuntimeException {

    public CannotCloseStatementException(final String message) {
        super(message);
    }
}
