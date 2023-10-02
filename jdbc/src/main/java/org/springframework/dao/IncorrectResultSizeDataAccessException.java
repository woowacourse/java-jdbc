package org.springframework.dao;

public class IncorrectResultSizeDataAccessException extends RuntimeException {
    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(int expectedSize) {
        super("Incorrect result size: expected " + expectedSize);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    public IncorrectResultSizeDataAccessException(final String message, final int expectedSize, final int actualSize) {
        super(message);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    public IncorrectResultSizeDataAccessException(final String message, final Throwable cause, final int expectedSize, final int actualSize) {
        super(message, cause);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }
}
