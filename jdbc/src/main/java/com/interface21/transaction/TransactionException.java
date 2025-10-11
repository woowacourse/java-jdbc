package com.interface21.transaction;

public class TransactionException extends RuntimeException {

    public TransactionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
