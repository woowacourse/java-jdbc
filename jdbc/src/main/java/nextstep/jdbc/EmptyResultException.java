package nextstep.jdbc;

public class EmptyResultException extends DataAccessException {

    private static final String MESSAGE = "데이터가 없습니다.";

    public EmptyResultException() {
        super(MESSAGE);
    }

    public EmptyResultException(String message) {
        super(message);
    }

}
