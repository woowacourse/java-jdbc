package org.springframework.jdbc.exception;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {
    public EmptyResultDataAccessException(final int i) {
        super(i,0);
    }
}
