package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface JdbcMapper<T> {

    T mapRow(ResultSet resultSet) throws SQLException;
}
