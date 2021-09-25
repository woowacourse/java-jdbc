package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.exception.DataAccessException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {
    T doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException;
}
