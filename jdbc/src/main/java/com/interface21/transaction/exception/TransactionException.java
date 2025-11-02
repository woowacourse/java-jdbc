package com.interface21.transaction.exception;

public class TransactionException extends RuntimeException {

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
