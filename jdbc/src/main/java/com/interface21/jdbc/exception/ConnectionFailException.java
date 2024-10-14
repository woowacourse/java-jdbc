package com.interface21.jdbc.exception;

import com.interface21.dao.DataAccessException;

public class ConnectionFailException extends DataAccessException {
    public ConnectionFailException(final String sql, final Throwable cause) {
        super(String.format("DB 연결중 실패 - 쿼리: [%s]. 에러: [%s]",sql,cause.getMessage()), cause);
    }
}
