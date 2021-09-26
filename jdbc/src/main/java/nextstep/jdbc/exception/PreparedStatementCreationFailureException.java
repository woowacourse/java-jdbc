package nextstep.jdbc.exception;

public class PreparedStatementCreationFailureException extends DataAccessException {

    public PreparedStatementCreationFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
