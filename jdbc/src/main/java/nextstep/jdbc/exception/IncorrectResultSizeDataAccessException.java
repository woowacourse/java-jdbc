package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    private final int expectedSize;

    private final int actualSize;

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }
}
