package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.resultset.ResultSetExecutor;
import nextstep.jdbc.resultset.RowMapper;
import nextstep.jdbc.resultset.RowMapperResultSetExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            statement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a sql.", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExecutor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, new RowMapperResultSetExecutor<>(rowMapper), args).get(0);
    }

    private <T> T query(final String sql, final ResultSetExecutor<T> executor, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }

            return extractData(statement, executor);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a sql.", e);
        }
    }

    private <T> T extractData(final PreparedStatement statement, final ResultSetExecutor<T> executor) {
        try (final ResultSet resultSet = statement.executeQuery()) {
            return executor.extractData(resultSet);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a sql.", e);
        }
    }
}
