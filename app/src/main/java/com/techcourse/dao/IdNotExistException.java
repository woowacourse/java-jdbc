package com.techcourse.dao;

public class IdNotExistException extends RuntimeException {

    private static final String ERROR_MESSAGE = "해당 아이디를 가진 유저가 존재하지 않습니다.";

    public IdNotExistException() {
        super(ERROR_MESSAGE);
    }
}
