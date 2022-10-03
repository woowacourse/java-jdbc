package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        super(expectedSize + "개의 데이터가 예상되었지만, " + actualSize + "개의 데이터가 존재합니다.");
    }
}
