package nextstep.jdbc.exception;

import java.sql.SQLException;

public class ResultSetMappingFailureException extends DataAccessException {

    public ResultSetMappingFailureException(SQLException exception) {
        super(exception);
    }
}
