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
    private static final PreparedStatementSetter STATEMENT_SETTER = new PreparedStatementSetter();
    private final DataSource dataSource;

    public JdbcExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T find(final String sql, final ResultSetCallback<T> resultSetCallback, final Object... args) {
        return executeOrThrow(sql, statement -> {
            try (final ResultSet rs = statement.executeQuery()) {
                return resultSetCallback.execute(rs);
            }
        }, args);
    }

    public Integer update(final String sql, final Object... args) {
        return executeOrThrow(sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T executeOrThrow(final String sql, final PreparedStatementCallback<T> statementCallback,
                                 final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement statement = STATEMENT_SETTER.set(conn.prepareStatement(sql), args)) {
            return statementCallback.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
