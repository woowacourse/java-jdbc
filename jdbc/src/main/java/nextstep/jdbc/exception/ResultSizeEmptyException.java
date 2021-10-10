package nextstep.jdbc.exception;

public class ResultSizeEmptyException extends DataAccessException {

    public ResultSizeEmptyException() {
        super("Result of query expected to be single, but is empty.");
    }
}
