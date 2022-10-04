package nextstep.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.dao.DataAccessException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T doInPreparedStatement(final PreparedStatement preparedStatement) throws SQLException, DataAccessException;
}
