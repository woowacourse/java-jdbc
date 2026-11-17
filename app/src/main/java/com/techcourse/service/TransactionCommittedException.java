package com.techcourse.service;

/**
 * The transaction committed successfully, but connection cleanup failed.
 * Callers must not retry the business operation in response to this exception.
 */
public class TransactionCommittedException extends RuntimeException {

    public TransactionCommittedException(final Throwable cause) {
        super("Transaction committed successfully, but connection cleanup failed; do not retry", cause);
    }
}
