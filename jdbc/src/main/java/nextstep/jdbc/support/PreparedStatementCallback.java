package nextstep.jdbc.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.exception.DataAccessException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {
    T doInStatement(PreparedStatement pstmt) throws SQLException, DataAccessException;
}
