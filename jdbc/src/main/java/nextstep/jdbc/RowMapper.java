package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {

    T mapLow(final ResultSet resultSet, final int rowNum) throws SQLException;
}
