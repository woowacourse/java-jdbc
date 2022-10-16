package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T doPreparedStatement(final PreparedStatement pstmt) throws SQLException;
}
