package com.interface21.dao;

public class RevertAutoCommitFailException extends DataAccessException {

    public RevertAutoCommitFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
