package nextstep.jdbc.exception;

public class ResultSizeOverflowException extends DataAccessException {

    public ResultSizeOverflowException(int size) {
        super(String.format("Result of query expected to be single, but contains more than one. actual : %d", size));
    }
}
