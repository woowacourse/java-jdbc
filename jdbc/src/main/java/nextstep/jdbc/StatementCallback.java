package nextstep.jdbc;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface StatementCallback<T> {

    T doInStatement(final PreparedStatement pstmt) throws DataAccessException;
}
