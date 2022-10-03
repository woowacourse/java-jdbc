package nextstep.jdbc;

public class EmptyResultDataAccessException extends RuntimeException {

    private static final String MESSAGE = "Incorrect result size: expected 1 but 0";

    public EmptyResultDataAccessException() {
        super(MESSAGE);
    }
}
