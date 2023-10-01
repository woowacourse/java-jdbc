package com.techcourse.dao.exception;

public class UserFoundException extends RuntimeException {

    public UserFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
