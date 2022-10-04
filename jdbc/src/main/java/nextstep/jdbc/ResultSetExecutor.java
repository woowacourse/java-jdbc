package nextstep.jdbc;

import java.sql.Connection;

public interface ResultSetExecutor {

    Object execute(Connection connection, String sql, Object[] columns);
}
