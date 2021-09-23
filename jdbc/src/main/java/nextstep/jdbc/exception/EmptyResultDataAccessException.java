package nextstep.jdbc.exception;

public class EmptyResultDataAccessException extends RuntimeException {

    public EmptyResultDataAccessException() {
        super("expected result at least one row but zero rows returned");
    }
}
