package nextstep.jdbc.exception;

public class NotFoundDataException extends RuntimeException {

    public NotFoundDataException() {
        super("데이터가 존재하지 않습니다.");
    }
}
