package com.interface21.jdbc.exception;

public class DatabaseConnectionFailException extends RuntimeException {

    private String sqlState;
    private String message;

    public DatabaseConnectionFailException(String sqlState, String message) {
        this.sqlState = sqlState;
        this.message = message;
    }

    public DatabaseConnectionFailException(String message) {
        this.message=message;
    }
}
