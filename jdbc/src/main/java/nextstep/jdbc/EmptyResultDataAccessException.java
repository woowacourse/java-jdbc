package nextstep.jdbc;

public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException(final String message) {
        super(message);
    }
}
