package com.techcourse.domain;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(final long id) {
        super("존재하지 않는 사용자입니다. id=" + id);
    }
}
