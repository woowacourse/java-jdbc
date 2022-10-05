package nextstep.mvc.exception;

public class HandlerAdapterException extends RuntimeException {

    public HandlerAdapterException(final String message) {
        super(message);
    }

    public HandlerAdapterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
