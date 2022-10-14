package nextstep.jdbc;

public class JdbcExecuteException extends RuntimeException {

    public JdbcExecuteException() {
        super();
    }

    public JdbcExecuteException(String message) {
        super(message);
    }
}
