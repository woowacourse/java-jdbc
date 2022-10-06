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

    public <T> T updateOrThrow(final String sql, final PreparedStatementCallback<T> preparedStatementCallback, final Object... args) {
        try {
            return preparedStatementCallback.execute(getStatement(sql, args));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T findOrThrow(final String sql, final ResultSetCallback<T> resultSetCallback, final Object... args) {
        try (final ResultSet rs = getStatement(sql, args).executeQuery()) {
            return resultSetCallback.execute(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getStatement(final String sql, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement stmt = STATEMENT_SETTER.set(conn.prepareStatement(sql), args)) {
            return stmt;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
