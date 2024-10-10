package com.interface21.jdbc.core;

public class SQLExecuteException extends RuntimeException {

    public SQLExecuteException(String message) {
        super(message);
    }

    public SQLExecuteException(String message, Throwable cause) {
        super(message, cause);
    }
}
