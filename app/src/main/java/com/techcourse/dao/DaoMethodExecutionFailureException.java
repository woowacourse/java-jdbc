package com.techcourse.dao;

public class DaoMethodExecutionFailureException extends RuntimeException {

    public DaoMethodExecutionFailureException(String message) {
        super(message);
    }
}
