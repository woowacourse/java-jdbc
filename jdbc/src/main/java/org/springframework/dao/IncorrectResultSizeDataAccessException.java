package org.springframework.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(final int expected, final int actual) {
        super(String.format("의도한 것과 다른 개수의 데이터가 조회되었습니다. Expected: %d, Actual: %d", expected, actual));
    }
}
