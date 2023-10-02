package org.springframework.jdbc.exception;

public class DatabaseExecuteException extends RuntimeException {

    public DatabaseExecuteException(String message) {
        super(message);
    }
}
