package nextstep.jdbc.exception;

public class EmptyResultException extends RuntimeException {

    private static final String MESSAGE = "데이터가 존재하지 않습니다.";

    public EmptyResultException() {
        super(MESSAGE);
    }
}
