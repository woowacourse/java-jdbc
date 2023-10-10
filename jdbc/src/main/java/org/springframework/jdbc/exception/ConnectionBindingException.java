package org.springframework.jdbc.exception;

public class ConnectionBindingException extends RuntimeException {

    public ConnectionBindingException(String message) {
        super(message);
    }
}
