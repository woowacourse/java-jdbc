package org.springframework.dao;

public class TransactionAutoCommitException extends RuntimeException {

    public TransactionAutoCommitException() {
        super();
    }

    public TransactionAutoCommitException(final String message) {
        super(message);
    }

    public TransactionAutoCommitException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TransactionAutoCommitException(final Throwable cause) {
        super(cause);
    }

    public TransactionAutoCommitException(final String message, final Throwable cause, final boolean enableSuppression,
                                          final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
