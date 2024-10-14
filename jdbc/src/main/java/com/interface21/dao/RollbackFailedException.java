package com.interface21.dao;

public class RollbackFailedException extends DataAccessException {

    public RollbackFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
