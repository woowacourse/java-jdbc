package nextstep.jdbc.exception;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(SQLException exception) {
        super(exception);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
