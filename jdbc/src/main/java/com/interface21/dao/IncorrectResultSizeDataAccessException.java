package com.interface21.dao;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IncorrectResultSizeDataAccessException() {
        super();
    }

    public IncorrectResultSizeDataAccessException(Throwable cause) {
        super(cause);
    }
}
