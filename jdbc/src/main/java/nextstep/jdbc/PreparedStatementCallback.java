package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T doInPreparedStatement(PreparedStatement statement) throws SQLException;
}
