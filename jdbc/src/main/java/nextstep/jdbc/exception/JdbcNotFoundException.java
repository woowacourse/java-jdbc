package nextstep.jdbc.exception;

public class JdbcNotFoundException extends RuntimeException {

    public JdbcNotFoundException(final String message) {
        super(message);
    }
}
