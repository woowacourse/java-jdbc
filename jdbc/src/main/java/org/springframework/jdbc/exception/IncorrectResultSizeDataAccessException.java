package org.springframework.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException{

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }

}
