package com.interface21.jdbc.exception;

public class ConnectionFailException extends DatabaseAccessException {
    public ConnectionFailException(final String sql, final Throwable cause) {
        super(String.format("DB 연결중 실패 - 쿼리: [%s]. 에러: [%s]",sql,cause.getMessage()), cause);
    }
}
