package org.springframework.jdbc.exception;

public class DataUpdateException extends RuntimeException {

    public DataUpdateException(String message) {
        super(message);
    }
}
