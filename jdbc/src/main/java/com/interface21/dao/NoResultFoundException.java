package com.interface21.dao;

public class NoResultFoundException extends DataAccessException {
    public NoResultFoundException() {
        super("조회 결과를 찾을 수 없습니다.");
    }
}
