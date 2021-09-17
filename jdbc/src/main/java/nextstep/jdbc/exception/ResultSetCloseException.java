package nextstep.jdbc.exception;

public class ResultSetCloseException extends RuntimeException {

    public ResultSetCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
