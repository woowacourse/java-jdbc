package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcStrategy {

    Object apply(PreparedStatement preparedStatement) throws SQLException;
}
