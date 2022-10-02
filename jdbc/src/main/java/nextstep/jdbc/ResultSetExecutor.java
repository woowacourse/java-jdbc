package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetExecutor<T> {

    T extractData(ResultSet resultSet) throws SQLException, DataAccessException;
}
