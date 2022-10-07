package nextstep.jdbc.exception;

public class DataTypeNotSupportedException extends RuntimeException{

    private static final String MESSAGE = "지원하지 않는 데이터 타입입니다!";

    public DataTypeNotSupportedException() {
        super(MESSAGE);
    }
}
