package com.interface21.jdbc.exception;

public class TransactionRollbackException extends RuntimeException {

    public TransactionRollbackException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
