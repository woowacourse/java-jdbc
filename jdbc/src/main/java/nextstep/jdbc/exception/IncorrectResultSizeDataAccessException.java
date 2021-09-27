package nextstep.jdbc.exception;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException(final String sql) {
        super(String.format("조건을 만족하는 행이 복수 입니다.%nsql:(%s)%n", sql));
    }
}
