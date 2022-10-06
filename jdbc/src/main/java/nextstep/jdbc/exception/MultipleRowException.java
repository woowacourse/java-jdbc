package nextstep.jdbc.exception;

public class MultipleRowException extends DataAccessException {

    public MultipleRowException() {
        super("데이터가 여러 개 입니다.");
    }
}
