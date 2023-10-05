package org.springframework.dao;

public class IncorrectResultSizeDataAccessException extends DataAccessException{

    private static final int EXPECT_SIZE = 1;

    public IncorrectResultSizeDataAccessException(final int actualSize) {
        super("Incorrect result size: expected " + EXPECT_SIZE + ", actual " + actualSize);
    }
}
