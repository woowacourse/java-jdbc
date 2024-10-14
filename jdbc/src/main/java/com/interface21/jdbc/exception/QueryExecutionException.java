package com.interface21.jdbc.exception;

import com.interface21.dao.DataAccessException;

import java.sql.PreparedStatement;

public class QueryExecutionException extends DataAccessException {
    public QueryExecutionException(final PreparedStatement pstmt, final Throwable cause) {
        super(String.format("쿼리 실행중 실패 - 쿼리: [%s]. 에러: [%s]", pstmt.toString(), cause.getMessage()), cause);
    }
}
