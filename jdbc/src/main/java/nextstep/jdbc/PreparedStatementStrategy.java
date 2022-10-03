package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementStrategy<T> {

    T doStatement(PreparedStatement pstmt) throws SQLException;
}
