package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetMapper<T> {

    T mapToResult(ResultSet resultSet) throws SQLException;
}
