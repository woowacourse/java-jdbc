package nextstep.jdbc.exception;

public class SQLAccessException extends RuntimeException{

    private static final String MESSAGE = "SQL에서 예외가 발생했습니다";

    public SQLAccessException() {
        super(MESSAGE);
    }
}
