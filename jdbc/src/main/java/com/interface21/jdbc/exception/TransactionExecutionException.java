package com.interface21.jdbc.exception;

public class TransactionExecutionException extends RuntimeException {

    public TransactionExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
