package com.interface21.dao;

/**
 * 예상한 결과 개수와 다를 때 발생하는 예외
 */
public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private static final long serialVersionUID = 1L;
    
    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    public IncorrectResultSizeDataAccessException(String message, int expectedSize, int actualSize) {
        super(message);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    public int getExpectedSize() {
        return expectedSize;
    }

    public int getActualSize() {
        return actualSize;
    }
}
