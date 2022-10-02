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

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void executeQuery(final PreparedStatementExecutor executor) {
        try (Connection connection = getConnection()) {
            executor.execute(connection);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<?> executeQueryForList(final ResultSetExecutor<?> executor) {
        try (Connection connection = getConnection()) {
            return executor.execute(connection);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object executeQueryForObject(final SingleResultSetExecutor<?> executor, final Object[] columns) {
        try (Connection connection = getConnection()) {
            return executor.execute(connection, columns);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
