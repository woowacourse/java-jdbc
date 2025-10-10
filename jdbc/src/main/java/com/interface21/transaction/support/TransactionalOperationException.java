package com.interface21.transaction.support;

public class TransactionalOperationException extends RuntimeException {
    public TransactionalOperationException(Exception e) {
        super(e);
    }
}
