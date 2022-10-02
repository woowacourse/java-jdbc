package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetWrapper<T> {

    T execute(ResultSet resultSet) throws SQLException;
}
