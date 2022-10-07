package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface JdbcExecutor<T> {

    T execute(PreparedStatement statement) throws SQLException;
}
