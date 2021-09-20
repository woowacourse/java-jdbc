package com.techcourse.exception;

public class SqlInitException extends RuntimeException {

    public SqlInitException(Throwable cause) {
        super(cause.getMessage());
    }
}
