package com.interface21.jdbc.exception;

public class QueryParseException extends RuntimeException {
    public QueryParseException(final String message, final Throwable cause) {
        super(message,cause);
    }
}
