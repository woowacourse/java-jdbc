package nextstep.jdbc.exception;

public class InvalidQueryParameterException extends RuntimeException {

    public InvalidQueryParameterException() {
        super("요청 쿼리에 올바르지 않은 파라미터입니다.");
    }
}
