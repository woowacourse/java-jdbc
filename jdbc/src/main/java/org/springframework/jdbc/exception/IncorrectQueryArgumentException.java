package org.springframework.jdbc.exception;

public class IncorrectQueryArgumentException extends RuntimeException {

    public IncorrectQueryArgumentException(String message) {
        super(message);
    }

}
