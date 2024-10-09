package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

public class NotSingleResultDataAccessException extends DataAccessException {

    private static final String ERROR_MESSAGE = "2개 이상의 결과가 검색되었습니다";

    public NotSingleResultDataAccessException() {
        super(ERROR_MESSAGE);
    }
}
