package com.techcourse.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String account) {
        super(String.format("User not found. account: %s", account));
    }

    public UserNotFoundException(Long id) {
        super(String.format("User not found. id: %d", id));
    }
}
