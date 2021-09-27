package nextstep.jdbc.exception;

public class EmptyResultDataAccessException extends RuntimeException{
    private static final String MESSAGE = "쿼리 수행 후 결과가 없습니다.";
    public EmptyResultDataAccessException() {
        super(MESSAGE);
    }
}
