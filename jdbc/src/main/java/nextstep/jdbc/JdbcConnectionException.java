package nextstep.jdbc;

public class JdbcConnectionException extends RuntimeException {

    public JdbcConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
