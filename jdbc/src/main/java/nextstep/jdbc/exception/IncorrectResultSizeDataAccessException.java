package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException {
    public IncorrectResultSizeDataAccessException(final String message) {
        super(message);
    }
}
