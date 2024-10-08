package com.techcourse.dao;

public class AccountNotExistException extends RuntimeException {

    private static final String ERROR_MESSAGE = "해당 계정을 사용하는 유저가 없습니다.";

    public AccountNotExistException() {
        super(ERROR_MESSAGE);
    }
}
