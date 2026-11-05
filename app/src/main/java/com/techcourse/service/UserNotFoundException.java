package com.techcourse.service;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(final long id) {
        super("User not found: " + id);
    }
}
