package org.springframework.dao;

public class IncorrectResultSizeException extends RuntimeException {

    public IncorrectResultSizeException(String message) {
        super(message);
    }
}
