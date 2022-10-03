package nextstep.jdbc;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    private static final String MESSAGE = "Incorrect result size: expected 1 but ";

    public IncorrectResultSizeDataAccessException(final int actualResultSize) {
        super(MESSAGE + actualResultSize);
    }
}
