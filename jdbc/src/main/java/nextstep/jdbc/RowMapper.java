package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    T rowMap(ResultSet resultSet, int rowNum) throws SQLException;
}
