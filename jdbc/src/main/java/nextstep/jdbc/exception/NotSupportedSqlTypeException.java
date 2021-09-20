package nextstep.jdbc.exception;

public class NotSupportedSqlTypeException extends RuntimeException {

    public NotSupportedSqlTypeException() {
    }

    public NotSupportedSqlTypeException(String message) {
        super(message);
    }

    public NotSupportedSqlTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
