package nextstep.exception;

public class SqlQueryException extends RuntimeException {

    public SqlQueryException() {
    }

    public SqlQueryException(Exception e) {
        super(e.getMessage());
    }
}
