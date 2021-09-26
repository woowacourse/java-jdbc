package nextstep.jdbc.exception;

public class DatabaseConnectionFailureException extends DataAccessException {

    public DatabaseConnectionFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
