package nextstep.jdbc.exception;

import java.sql.SQLException;

public class QueryExecutionFailureException extends DataAccessException {

    public QueryExecutionFailureException(SQLException exception) {
        super(exception);
    }
}
