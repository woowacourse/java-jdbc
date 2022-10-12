package nextstep.jdbc.exception;

public class NullException extends DataAccessException {

    public NullException(final String name) {
        super(name + " must be not null.");
    }
}
