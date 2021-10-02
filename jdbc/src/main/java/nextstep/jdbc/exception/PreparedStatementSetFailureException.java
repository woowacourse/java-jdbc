package nextstep.jdbc.exception;

import java.sql.SQLException;

public class PreparedStatementSetFailureException extends DataAccessException {

    public PreparedStatementSetFailureException(SQLException exception) {
        super(exception);
    }
}
