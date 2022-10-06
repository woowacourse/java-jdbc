package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ObjectMapper<T> {

    T mapObject(final ResultSet resultSet) throws SQLException;
}
