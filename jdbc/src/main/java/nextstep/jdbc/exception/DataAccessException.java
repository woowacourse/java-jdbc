package nextstep.jdbc.exception;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(SQLException exception) {
        super(exception.getMessage(), exception.getCause());
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
