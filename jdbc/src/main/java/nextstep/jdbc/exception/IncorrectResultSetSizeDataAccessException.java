package nextstep.jdbc.exception;

public class IncorrectResultSetSizeDataAccessException extends RuntimeException {
    private static final String MESSAGE = "result set size should be 1";

    public IncorrectResultSetSizeDataAccessException() {
        super(MESSAGE);
    }
}
