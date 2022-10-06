package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapperCallback<T, R> {

    R doInRowMapper(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException;
}
