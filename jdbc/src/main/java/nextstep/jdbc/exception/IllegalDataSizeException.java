package nextstep.jdbc.exception;

public class IllegalDataSizeException extends RuntimeException {

    public IllegalDataSizeException() {
        super("데이터의 사이즈가 적절하지 않습니다.");
    }
}
