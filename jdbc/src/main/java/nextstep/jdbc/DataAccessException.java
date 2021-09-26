package nextstep.jdbc;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(SQLException exception) {
        super(exception.getMessage(), exception.getCause());
    }
}
