package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SelectQueryExecutor<T> {

    T executeQuery(ResultSet resultSet) throws SQLException;
}
