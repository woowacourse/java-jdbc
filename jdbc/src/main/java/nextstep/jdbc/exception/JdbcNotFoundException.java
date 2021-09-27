package nextstep.jdbc.exception;

public class JdbcNotFoundException extends RuntimeException {

    public JdbcNotFoundException(final String sql) {
        super(String.format("조건을 만족하는 행을 찾지 못했습니다.%nsql:(%s)%n", sql));
    }
}
