package nextstep.jdbc.element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcExecutor {

    private static final Logger log = LoggerFactory.getLogger(JdbcExecutor.class);

    private final DataSource dataSource;

    public JdbcExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T find(final Connection connection, String sql, PreparedStatementSetter statementSetter,
                      ResultSetCallback<T> resultSetCallback) {
        return executeOrThrow(connection, sql, statementSetter, statement -> {
            try (final ResultSet rs = statement.executeQuery()) {
                return resultSetCallback.execute(rs);
            }
        });
    }

    public Integer update(final Connection connection, final String sql,
                          final PreparedStatementSetter statementSetter) {
        return executeOrThrow(connection, sql, statementSetter, PreparedStatement::executeUpdate);
    }

    private <T> T executeOrThrow(final Connection connection, final String sql, final PreparedStatementSetter setter,
                                 final PreparedStatementCallback<T> statementCallback) {
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return statementCallback.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
