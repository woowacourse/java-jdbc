package nextstep.jdbc.exception;

public class EmptyResultSetDataAccessException extends RuntimeException {
    private static final String MESSAGE = "result set is EMPTY";

    public EmptyResultSetDataAccessException() {
        super(MESSAGE);
    }
}
