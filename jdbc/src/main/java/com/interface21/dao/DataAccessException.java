package com.interface21.dao;

public class DataAccessException extends RuntimeException {

    public DataAccessException() {
        super("데이터 접근 중 오류가 발생했습니다.");
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
