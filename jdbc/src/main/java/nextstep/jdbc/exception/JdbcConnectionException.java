package nextstep.jdbc.exception;

public class JdbcConnectionException extends RuntimeException {

    public JdbcConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
