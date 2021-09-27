package nextstep.jdbc;

public class JdbcCustomException extends RuntimeException {
    public JdbcCustomException(String message) {
        super(message);
    }
}
