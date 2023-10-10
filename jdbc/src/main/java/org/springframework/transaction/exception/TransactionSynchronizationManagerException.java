package org.springframework.transaction.exception;

public class TransactionSynchronizationManagerException extends RuntimeException{

    public TransactionSynchronizationManagerException(final Throwable cause) {
        super(cause);
    }
}
