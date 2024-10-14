package com.interface21.dao;

public class TransactionRollbackException extends DataAccessException {

    public TransactionRollbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
