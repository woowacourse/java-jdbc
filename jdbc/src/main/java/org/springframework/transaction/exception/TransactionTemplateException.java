package org.springframework.transaction.exception;

public class TransactionTemplateException extends RuntimeException {

    public TransactionTemplateException(final Throwable cause) {
        super(cause);
    }
}
