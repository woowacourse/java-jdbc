package com.interface21.jdbc;

public class InvalidResultSetException extends RuntimeException {

    public InvalidResultSetException(final String msg) {
        super(msg);
    }

    public InvalidResultSetException(final String msg, final IllegalStateException ex) {
        super(msg, ex);
    }
}
