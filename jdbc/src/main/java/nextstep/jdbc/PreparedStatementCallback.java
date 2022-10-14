package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T execute(final PreparedStatement preparedStatement) throws SQLException;
}
