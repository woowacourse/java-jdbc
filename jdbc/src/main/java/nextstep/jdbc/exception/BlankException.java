package nextstep.jdbc.exception;

public class BlankException extends DataAccessException {

    public BlankException(final String name) {
        super(name + " must be not blank.");
    }
}
