package nextstep.jdbc.exception;

public class DataAccessException extends RuntimeException {

    public DataAccessException() {
    }

    public DataAccessException(final String message) {
        super(message);
    }
}
