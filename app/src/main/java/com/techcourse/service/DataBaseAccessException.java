package com.techcourse.service;

public class DataBaseAccessException extends RuntimeException {

    public DataBaseAccessException(String message) {
        super("데이터베이스에 조회할 수 없습니다. \n" + message);
    }
}
