package nextstep.exception;

public class JdbcInternalException extends DataAccessException{

    public JdbcInternalException(String message) {
        super(message);
    }

    public JdbcInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
