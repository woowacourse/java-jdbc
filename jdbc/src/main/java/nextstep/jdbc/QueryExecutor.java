package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface QueryExecutor<T> {
    T execute(PreparedStatement preparedStatement) throws SQLException;
}
