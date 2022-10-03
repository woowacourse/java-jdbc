package nextstep.jdbc;

public class EmptyResultDataAccessException extends DataAccessException{

    public EmptyResultDataAccessException(final int expectedSize) {
        super(expectedSize + "개의 데이터가 예상되었지만, 데이터가 존재하지 않습니다.");
    }
}
