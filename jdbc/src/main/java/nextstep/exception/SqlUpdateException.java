package nextstep.exception;

public class SqlUpdateException extends RuntimeException {

    public SqlUpdateException(Exception e) {
        super(e.getMessage());
    }
}
