package nextstep.jdbc.exception;

public class NotSingleResultDataException extends DataAccessException {
    public NotSingleResultDataException(String message) {
        super(message);
    }
}
