package nextstep.jdbc.transaction;

public class RollbackException extends RuntimeException {
    public RollbackException(RuntimeException e) {
        super(e);
    }
}
