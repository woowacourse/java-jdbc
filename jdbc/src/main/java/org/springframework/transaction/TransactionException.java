package org.springframework.transaction;

public class TransactionException extends RuntimeException {

    public TransactionException(Exception e) {
        super(e);
    }

}
