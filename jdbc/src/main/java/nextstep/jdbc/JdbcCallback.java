package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;


@FunctionalInterface
public interface JdbcCallback<T> {

    T call(final PreparedStatement preparedStatement) throws SQLException, DataAccessException;
}
