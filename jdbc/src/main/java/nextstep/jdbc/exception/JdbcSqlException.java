package nextstep.jdbc.exception;

public class JdbcSqlException extends RuntimeException {

    public JdbcSqlException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
