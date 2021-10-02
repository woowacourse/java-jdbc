package nextstep.jdbc.exception;

public class InvalidSQLMethodException extends DataAccessException {

    public InvalidSQLMethodException(String method, String sql) {
        super(String.format("\"%s\" query is not for [%s] method", sql, method));
    }
}
