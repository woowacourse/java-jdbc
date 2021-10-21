package nextstep.exception;

public class IncorrectResultSizeDataAccessException extends DataAccessException{

    public IncorrectResultSizeDataAccessException() {
        super("결과값의 크기가 맞지 않습니다.");
    }
}
