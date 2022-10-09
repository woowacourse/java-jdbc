package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcAction<T> {
    T doAction(PreparedStatement statement) throws SQLException;
}
