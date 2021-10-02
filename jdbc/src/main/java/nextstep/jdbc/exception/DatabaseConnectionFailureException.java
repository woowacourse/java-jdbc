package nextstep.jdbc.exception;

import java.sql.SQLException;

public class DatabaseConnectionFailureException extends DataAccessException {

    public DatabaseConnectionFailureException(SQLException exception) {
        super(exception);
    }
}
