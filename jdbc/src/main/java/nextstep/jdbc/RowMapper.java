package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    T rowMap(final ResultSet resultSet, final int rowNum) throws SQLException;
}
