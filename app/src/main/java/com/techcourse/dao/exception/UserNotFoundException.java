package com.techcourse.dao.exception;

public class UserNotFoundException extends IllegalArgumentException {

    public UserNotFoundException(final String message) {
        super(message);
    }
}
