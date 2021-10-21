package nextstep.jdbc;

public class TooManyResultsException extends DataAccessException {

    private static final String MESSAGE = "일치하는 데이터가 2개 이상입니다.";

    public TooManyResultsException() {
        super(MESSAGE);
    }

    public TooManyResultsException(String message) {
        super(message);
    }
}
