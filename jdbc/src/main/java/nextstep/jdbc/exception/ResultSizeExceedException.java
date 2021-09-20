package nextstep.jdbc.exception;

public class ResultSizeExceedException extends DataAccessException {

    public ResultSizeExceedException() {
    }

    public ResultSizeExceedException(String message) {
        super(message);
    }

    public ResultSizeExceedException(String message, Throwable cause) {
        super(message, cause);
    }
}
