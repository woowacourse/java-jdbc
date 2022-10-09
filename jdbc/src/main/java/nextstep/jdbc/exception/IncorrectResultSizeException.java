package nextstep.jdbc.exception;

public class IncorrectResultSizeException extends RuntimeException {

    private static final String MESSAGE = "하나의 데이터만 존재해야 합니다.";

    public IncorrectResultSizeException() {
        super(MESSAGE);
    }
}
