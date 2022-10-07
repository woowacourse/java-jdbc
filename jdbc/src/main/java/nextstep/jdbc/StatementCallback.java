package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementCallback<T> {

    T doInStatement(PreparedStatement stmt) throws DataAccessException, SQLException;
}
