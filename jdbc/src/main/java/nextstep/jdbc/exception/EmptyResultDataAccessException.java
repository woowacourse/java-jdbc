package nextstep.jdbc.exception;

public class EmptyResultDataAccessException extends RuntimeException{
    public EmptyResultDataAccessException(final String message) {
        super(message);
    }
}
