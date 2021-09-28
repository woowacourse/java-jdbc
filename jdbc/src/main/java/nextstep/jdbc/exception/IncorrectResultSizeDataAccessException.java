package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException() {
        super("컬렉션이 null 이거나 1개 이상의 element 보유");
    }

    public IncorrectResultSizeDataAccessException(int size) {
        super("데이터 결과가 1개 이상입니다. 결과 : " + size);
    }
}
