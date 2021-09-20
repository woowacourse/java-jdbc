package nextstep.jdbc.exception;

public class NotSupportedSqlTypeException extends DataAccessException {

    public NotSupportedSqlTypeException() {
    }

    public NotSupportedSqlTypeException(String message) {
        super(message);
    }

    public NotSupportedSqlTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
