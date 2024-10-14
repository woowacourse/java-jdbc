package com.interface21.jdbc.exception;

import com.interface21.dao.DataAccessException;

public class StatementSetException extends DataAccessException {
    public StatementSetException(final int index, final Object param, final Throwable cause) {
        super(String.format("파라미터 실행중 실패 - %d 번째 %s 에 대한 파라미터 설정을 실패했습니다.", index, param), cause);
    }
}
