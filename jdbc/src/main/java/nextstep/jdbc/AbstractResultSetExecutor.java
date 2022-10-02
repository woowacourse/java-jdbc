package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractResultSetExecutor<T> implements ResultSetExecutor {

    @Override
    public T execute(final Connection connection, final String sql, final Object[] columns) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return executeQuery(preparedStatement, columns);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract T executeQuery(final PreparedStatement preparedStatement,
                                      final Object[] columns) throws SQLException;
}
