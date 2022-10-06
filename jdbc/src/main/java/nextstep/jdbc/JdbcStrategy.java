package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcStrategy {

    Object apply(ResultSet resultSet) throws SQLException;
}
