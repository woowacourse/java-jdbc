package org.springframework.jdbc.exception;

public class RollbackFailException extends RuntimeException{

    public RollbackFailException(String message) {
        super(message);
    }
}
