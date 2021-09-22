package nextstep.jdbc;

public class DataAccessException extends RuntimeException {

    private static final String MESSAGE = "데이터 접근 오류가 발생했습니다.";

    public DataAccessException() {
        super(MESSAGE);
    }

    public DataAccessException(String message) {
        super(message);
    }

}
