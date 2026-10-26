package com.techcourse.service;

public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UserNotFoundException(final long userId) {
        super("User not found: " + userId);
    }
}
