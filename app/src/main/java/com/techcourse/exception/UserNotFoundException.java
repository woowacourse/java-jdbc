package com.techcourse.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("멤버를 찾을 수 없습니다.");
    }
}
