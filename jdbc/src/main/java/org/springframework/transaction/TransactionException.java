package org.springframework.transaction;

public class TransactionException extends RuntimeException {

    public TransactionException(final String message) {
        super(message);
    }
}
