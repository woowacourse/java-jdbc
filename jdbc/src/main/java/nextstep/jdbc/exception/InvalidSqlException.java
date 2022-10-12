package nextstep.jdbc.exception;

public class InvalidSqlException extends DataAccessException {

    public InvalidSqlException(String message) {
        super(message);
    }
}
