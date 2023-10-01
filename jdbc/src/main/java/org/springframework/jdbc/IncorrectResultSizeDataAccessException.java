package org.springframework.jdbc;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException(int size) {
        super("Incorrect Result Size Data returned! returned data size was " + size);
    }
}
