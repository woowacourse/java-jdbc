package com.interface21.jdbc.core;

public class TransactionRollbackException extends RuntimeException {

    public TransactionRollbackException(String message) {
        super(message);
    }

    public TransactionRollbackException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
