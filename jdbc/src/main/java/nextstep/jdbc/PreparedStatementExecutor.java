package nextstep.jdbc;

import java.sql.Connection;

public interface PreparedStatementExecutor {

    void execute(Connection connection);
}
