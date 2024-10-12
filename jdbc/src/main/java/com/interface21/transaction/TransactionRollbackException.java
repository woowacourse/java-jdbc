package com.interface21.transaction;

public class TransactionRollbackException extends RuntimeException {
    public TransactionRollbackException(Throwable cause) {
        super(cause);
    }
}
