package com.interface21.jdbc;

public class CannotExecuteQueryException extends RuntimeException {
    public CannotExecuteQueryException(String message) {
        super(message);
    }
}
