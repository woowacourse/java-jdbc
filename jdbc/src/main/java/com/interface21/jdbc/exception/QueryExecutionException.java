package com.interface21.jdbc.exception;

public class QueryExecutionException extends RuntimeException {
    public QueryExecutionException(final String message, final Throwable cause) {
        super(message,cause);
    }
}
