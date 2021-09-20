package nextstep.exception;

public class SqlQueryException extends RuntimeException {

    public SqlQueryException(Exception e) {
        super(e.getMessage());
    }
}
