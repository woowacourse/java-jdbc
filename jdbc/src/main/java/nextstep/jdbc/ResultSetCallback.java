package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetCallback<T> {

    T processResult(final ResultSet resultSet) throws SQLException;
}
