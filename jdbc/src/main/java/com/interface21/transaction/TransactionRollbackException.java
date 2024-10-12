package com.interface21.transaction;

public class TransactionRollbackException extends RuntimeException {

    public TransactionRollbackException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
