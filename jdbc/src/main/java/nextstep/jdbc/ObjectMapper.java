package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ObjectMapper<T> {

    T map(final ResultSet resultSet) throws SQLException;
}
