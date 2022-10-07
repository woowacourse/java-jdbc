package nextstep.jdbc.exception;

public class MultipleResultException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "조회 결과 값이 여러개 존재합니다.";

    public MultipleResultException() {
        super(DEFAULT_MESSAGE);
    }
}
