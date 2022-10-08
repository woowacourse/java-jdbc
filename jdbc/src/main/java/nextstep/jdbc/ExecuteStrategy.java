package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ExecuteStrategy<T> {

    T execute(final PreparedStatement pstmt) throws SQLException;
}
