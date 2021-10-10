package nextstep.jdbc.exception;

import java.sql.SQLException;

public class PreparedStatementCreationFailureException extends DataAccessException {

    public PreparedStatementCreationFailureException(SQLException exception) {
        super(exception);
    }
}
