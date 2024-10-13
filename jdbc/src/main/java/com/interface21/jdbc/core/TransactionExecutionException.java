package com.interface21.jdbc.core;

public class TransactionExecutionException extends RuntimeException {

    public TransactionExecutionException(String message) {
        super(message);
    }

    public TransactionExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
