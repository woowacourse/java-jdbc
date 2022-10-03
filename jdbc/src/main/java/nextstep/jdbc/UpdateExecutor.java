package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface UpdateExecutor<T> {

    T execute(PreparedStatement pstmt) throws SQLException;
}
