package org.springframework.transaction.exception;

public class TransactionManagerException extends RuntimeException {

    public TransactionManagerException(final String message) {
        super(message);
    }
}
