package nextstep.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeQuery(final PreparedStatementExecutor executor, final String sql) {
        try (Connection connection = getConnection()) {
            executor.execute(connection, sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<?> executeQueryForList(final ResultSetExecutor<?> executor, final String sql) {
        try (Connection connection = getConnection()) {
            return executor.execute(connection, sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object executeQueryForObject(final SingleResultSetExecutor<?> executor,
                                        final String sql,
                                        final Object[] columns) {
        try (Connection connection = getConnection()) {
            return executor.execute(connection, sql, columns);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
