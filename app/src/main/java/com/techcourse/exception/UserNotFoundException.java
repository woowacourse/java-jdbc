package com.techcourse.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String account) {
        super(String.format("유저가 존재하지 않습니다. account: %s", account));
    }

    public UserNotFoundException(Long id) {
        super(String.format("유저가 존재하지 않습니다. id: %d", id));
    }
}
