package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "Incorrect result size, expected : %s, actual : %s";

    public IncorrectResultSizeDataAccessException(int expected, int actual) {
        super(String.format(MESSAGE_FORMAT, expected, actual));
    }
}
