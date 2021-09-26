package nextstep.jdbc.exception;

public abstract class JdbcException extends RuntimeException {

    private static final String MESSAGE = "JdbcTemplate Occurred!!! Deatiled : ";

    public JdbcException(String message) {
        super(MESSAGE + message);
    }
}
