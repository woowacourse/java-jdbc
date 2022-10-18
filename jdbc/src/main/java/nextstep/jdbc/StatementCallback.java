package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementCallback<T> {

    T doInCallableStatement(final PreparedStatement pstmt) throws SQLException;
}
