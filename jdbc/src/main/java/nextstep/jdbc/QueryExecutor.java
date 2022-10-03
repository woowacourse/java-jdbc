package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface QueryExecutor<T> {

    T executeQuery(ResultSet resultSet) throws SQLException;
}
