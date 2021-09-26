package nextstep.jdbc.exception;

public class QueryExecutionFailureException extends DataAccessException {

    public QueryExecutionFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
