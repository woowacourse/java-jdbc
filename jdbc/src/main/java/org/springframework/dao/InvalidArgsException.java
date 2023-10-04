package org.springframework.dao;

public class InvalidArgsException extends DataAccessException {

    public InvalidArgsException(String message) {
        super(message);
    }
}
