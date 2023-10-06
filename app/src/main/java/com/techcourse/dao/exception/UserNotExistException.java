package com.techcourse.dao.exception;

public class UserNotExistException extends RuntimeException {

    public UserNotExistException() {
        super("존재하지 않는 사용자입니다.");
    }
}
