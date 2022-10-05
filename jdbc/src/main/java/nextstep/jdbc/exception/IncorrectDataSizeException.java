package nextstep.jdbc.exception;

public class IncorrectDataSizeException extends DataAccessException {

    public IncorrectDataSizeException(final int expectedSize, final int actualSize) {
        super(String.format("Incorrect result size. expected: %d, but actual: %d", expectedSize, actualSize));
    }
}
