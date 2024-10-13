package com.interface21.jdbc.core;

public class TransactionRollbackException extends RuntimeException {
    public TransactionRollbackException(Throwable cause) {
        super(cause);
    }
}
