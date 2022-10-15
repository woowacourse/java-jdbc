package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface JdbcMapper<T> {

    T mapRow(ResultSet resultSet) throws SQLException;
}
