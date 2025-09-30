package com.interface21.dao;

/**
 * 결과가 없을 때 발생하는 예외
 */
public class EmptyResultDataAccessException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public EmptyResultDataAccessException(String message) {
        super(message);
    }

    public EmptyResultDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
