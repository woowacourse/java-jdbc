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

    private static final int RESULT_SIZE_OF_ONE = 1;
    private static final int FIRST_INDEX_OF_RESULT = 0;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return statement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a ddl query.", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExecutor<>(rowMapper));
    }

    private <T> T query(final String sql, final ResultSetExecutor<T> executor, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }

            return executeQueryAndExtractData(statement, executor);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a select query.", e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> result = query(sql, new RowMapperResultSetExecutor<>(rowMapper), args);
        validateResultSize(result);
        return result.get(FIRST_INDEX_OF_RESULT);
    }

    private <T> void validateResultSize(final List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("A result is empty.");
        }
        if (result.size() > RESULT_SIZE_OF_ONE) {
            throw new DataAccessException("A result is over one.");
        }
    }

    private <T> T executeQueryAndExtractData(final PreparedStatement statement, final ResultSetExecutor<T> executor) {
        try (final ResultSet resultSet = statement.executeQuery()) {
            return executor.extractData(resultSet);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a sql.", e);
        }
    }
}
