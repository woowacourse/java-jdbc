package com.interface21.dao;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

    private static final long serialVersionUID = 1L;

    public EmptyResultDataAccessException(final int expectedSize) {
        super("조회 결과가 없습니다. expected=" + expectedSize, expectedSize, 0);
    }
}
