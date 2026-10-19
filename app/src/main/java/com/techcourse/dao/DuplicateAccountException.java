package com.techcourse.dao;

public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(String account, Throwable cause) {
        super("이미 사용 중인 계정입니다: " + account, cause);
    }
}
