package org.springframework.jdbc.exception;

public class DataAccessException extends RuntimeException{

    public DataAccessException(String message) {
        super(message);
    }
}
