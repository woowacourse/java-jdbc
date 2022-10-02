package nextstep.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface PreparedStatementExecutor {

    void execute(Connection connection, String sql) throws SQLException;
}
