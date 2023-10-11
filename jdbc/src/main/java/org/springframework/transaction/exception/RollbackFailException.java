package org.springframework.transaction.exception;

public class RollbackFailException extends RuntimeException {

    public RollbackFailException(final String message) {
        super(message);
    }
}
