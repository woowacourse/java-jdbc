package com.interface21.dao;

public class NotSingleResultException extends DataAccessException {
    public NotSingleResultException() {
        super("조회 결과가 둘 이상의 결과 값을 가지고 있습니다.");
    }
}
