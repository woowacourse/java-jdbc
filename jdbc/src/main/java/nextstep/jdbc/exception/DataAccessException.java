package nextstep.jdbc.exception;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(final SQLException exception) {
        super(exception);
    }
}
