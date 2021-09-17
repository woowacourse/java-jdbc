package com.techcourse.dao.jdbc.template.exception;

public class ExecuteQueryException extends RuntimeException {

    public ExecuteQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
