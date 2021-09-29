package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException {
    private static final String MESSAGE_FORMAT = "쿼리 실행 결과 로우가 1개가 아닙니다. (실제 = %d)";

    public IncorrectResultSizeDataAccessException(int number) {
        super(String.format(MESSAGE_FORMAT, number));
    }
}
