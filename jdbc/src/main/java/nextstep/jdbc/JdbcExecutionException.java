package nextstep.jdbc;

public class JdbcExecutionException extends RuntimeException {
    private static final String MESSAGE = "JdbcTemplate Execute Method Error Occurred!!!";

    public JdbcExecutionException() {
        super(MESSAGE);
    }
}
