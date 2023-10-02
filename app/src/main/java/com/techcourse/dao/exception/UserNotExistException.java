package com.techcourse.dao.exception;

public class UserNotExistException extends RuntimeException {

    public UserNotExistException(final Long userId) {
        super(String.format("존재하지 않는 사용자입니다. userId: %d", userId));
    }
}
