package org.springframework.dao;

public class TransactionCommitException extends RuntimeException {

    public TransactionCommitException() {
        super();
    }

    public TransactionCommitException(final String message) {
        super(message);
    }

    public TransactionCommitException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TransactionCommitException(final Throwable cause) {
        super(cause);
    }

    public TransactionCommitException(final String message, final Throwable cause, final boolean enableSuppression,
                                      final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
