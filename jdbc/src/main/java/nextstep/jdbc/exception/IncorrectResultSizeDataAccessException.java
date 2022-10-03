package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    private static final String ERROR_MESSAGE = "결과값은 오직 1개이어야만 합니다.";

    public IncorrectResultSizeDataAccessException() {
        super(ERROR_MESSAGE);
    }
}
