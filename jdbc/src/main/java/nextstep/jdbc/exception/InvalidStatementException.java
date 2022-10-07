package nextstep.jdbc.exception;

public class InvalidStatementException extends RuntimeException {

    private static final String MESSAGE = "정상적인 Statement가 아닙니다.";

    public InvalidStatementException() {
        super(MESSAGE);
    }
}
