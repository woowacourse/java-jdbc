package org.springframework.dao;

public class TransactionRollbackException extends RuntimeException {

    public TransactionRollbackException() {
        super();
    }

    public TransactionRollbackException(final String message) {
        super(message);
    }

    public TransactionRollbackException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TransactionRollbackException(final Throwable cause) {
        super(cause);
    }

    public TransactionRollbackException(final String message, final Throwable cause, final boolean enableSuppression,
                                        final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
