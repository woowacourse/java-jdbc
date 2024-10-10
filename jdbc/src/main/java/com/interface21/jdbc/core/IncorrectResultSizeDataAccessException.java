package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

public class IncorrectResultSizeDataAccessException extends DataAccessException {
    private static final String ERROR_MESSAGE_FORMAT = "데이터 결과 개수가 일치하지 않습니다. [기대 = %d 건, 실제 = %d 건]";

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super(ERROR_MESSAGE_FORMAT.formatted(expectedSize, actualSize));
    }
}
