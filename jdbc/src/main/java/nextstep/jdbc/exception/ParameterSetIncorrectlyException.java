package nextstep.jdbc.exception;

public class ParameterSetIncorrectlyException extends RuntimeException {

    public ParameterSetIncorrectlyException() {
    }

    public ParameterSetIncorrectlyException(String message) {
        super(message);
    }

    public ParameterSetIncorrectlyException(String message, Throwable cause) {
        super(message, cause);
    }
}
