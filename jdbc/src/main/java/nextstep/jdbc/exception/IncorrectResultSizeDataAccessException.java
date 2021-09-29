package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException() {
        super("expect a single row but getting zero or more than one rows");
    }
}
