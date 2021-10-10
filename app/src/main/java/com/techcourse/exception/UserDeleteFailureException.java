package com.techcourse.exception;

public class UserDeleteFailureException extends RuntimeException {

    public UserDeleteFailureException(Long id) {
        super(String.format("User delete failed. attempted delete id: %d", id));
    }
}
