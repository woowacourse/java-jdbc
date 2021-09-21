package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface JdbcCallback<T> {
    T call(final ResultSet resultSet) throws SQLException;
}
