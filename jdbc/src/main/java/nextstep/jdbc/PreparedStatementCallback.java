package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T doPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException;
}
