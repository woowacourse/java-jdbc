package nextstep.jdbc.exception;

public class ExecuteException extends DataAccessException {

    public ExecuteException(final Throwable cause) {
        super("Failed to execute a sql.", cause);
    }
}
