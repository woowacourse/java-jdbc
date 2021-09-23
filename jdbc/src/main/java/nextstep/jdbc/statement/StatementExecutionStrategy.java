package nextstep.jdbc.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementExecutionStrategy<T> {
    T apply(PreparedStatement pstmt) throws SQLException;
}
