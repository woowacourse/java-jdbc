package com.interface21.jdbc.exception;

public class DatabaseException extends RuntimeException {
    private static final String DATABASE_ERROR_MESSAGE = "Database error occurred while executing query.";

    public DatabaseException() {
        super(DATABASE_ERROR_MESSAGE);
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
