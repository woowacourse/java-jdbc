package nextstep.jdbc.exception;

public class NotFoundResultException extends RuntimeException {

    public NotFoundResultException(final String message) {
        super(message);
    }
}
