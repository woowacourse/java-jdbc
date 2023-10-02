package org.springframework.jdbc.exception;

public class InvalidDataSizeException extends RuntimeException {

    public InvalidDataSizeException(final String message) {
        super(message);
    }
}
