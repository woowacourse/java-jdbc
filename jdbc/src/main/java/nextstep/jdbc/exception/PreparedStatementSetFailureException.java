package nextstep.jdbc.exception;

public class PreparedStatementSetFailureException extends DataAccessException {

    public PreparedStatementSetFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
