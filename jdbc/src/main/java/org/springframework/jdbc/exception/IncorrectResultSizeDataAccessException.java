package org.springframework.jdbc.exception;

import java.sql.SQLException;

public class IncorrectResultSizeDataAccessException extends SQLException {

    private static final String MESSAGE_FORMAT = "%d개의 행이 조회됐습니다.";

    public IncorrectResultSizeDataAccessException(final int currentResultSize) {
        super(String.format(MESSAGE_FORMAT, currentResultSize));
    }
}
