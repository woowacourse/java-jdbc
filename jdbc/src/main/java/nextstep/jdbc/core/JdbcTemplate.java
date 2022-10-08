package nextstep.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.support.DataAccessUtils;
import nextstep.jdbc.support.ExecuteCallBack;
import nextstep.jdbc.support.ResultSetExecutor;
import nextstep.jdbc.support.RowMapper;
import nextstep.jdbc.support.RowMapperResultSetExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        DataAccessUtils.notBlank(sql, "SQL");

        return execute(sql, (statement -> {
            setParameters(args, statement);
            return statement.executeUpdate();
        }));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        DataAccessUtils.notBlank(sql, "SQL");
        DataAccessUtils.notNull(rowMapper, "RowMapper");

        return query(sql, new RowMapperResultSetExecutor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        DataAccessUtils.notBlank(sql, "SQL");
        DataAccessUtils.notNull(rowMapper, "RowMapper");

        final List<T> result = query(sql, new RowMapperResultSetExecutor<>(rowMapper), args);
        return DataAccessUtils.singleResult(result);
    }

    private <T> T query(final String sql, final ResultSetExecutor<T> executor, final Object... args) {
        return execute(sql, (statement -> {
            setParameters(args, statement);
            return executeQueryAndExtractData(statement, executor);
        }));
    }

    private void setParameters(final Object[] args, final PreparedStatement statement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
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

    private <T> T execute(final String sql, final ExecuteCallBack<T> callBack) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            return callBack.action(statement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a query.", e);
        }
    }
}
