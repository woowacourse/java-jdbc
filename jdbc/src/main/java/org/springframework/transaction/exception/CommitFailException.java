package org.springframework.transaction.exception;

public class CommitFailException extends RuntimeException {

    public CommitFailException(final String message) {
        super(message);
    }
}
