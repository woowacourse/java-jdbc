package nextstep.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.ExecuteException;
import nextstep.jdbc.support.DataAccessUtils;
import nextstep.jdbc.support.ExecuteCallBack;
import nextstep.jdbc.support.ResultSetExecutor;
import nextstep.jdbc.support.RowMapper;
import nextstep.jdbc.support.RowMapperResultSetExecutor;
import nextstep.jdbc.support.ValidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        ValidUtils.notBlank(sql, "SQL");

        return execute(sql, (statement -> {
            setParameters(args, statement);
            return statement.executeUpdate();
        }));
    }

    private <T> T execute(final String sql, final ExecuteCallBack<T> callBack) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            return callBack.action(statement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new ExecuteException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void setParameters(final Object[] args, final PreparedStatement statement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        ValidUtils.notBlank(sql, "SQL");
        ValidUtils.notNull(rowMapper, "RowMapper");

        return execute(sql, (statement -> {
            setParameters(args, statement);
            return executeQueryAndExtractData(statement, new RowMapperResultSetExecutor<>(rowMapper));
        }));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.singleResult(results);
    }

    private <T> T executeQueryAndExtractData(final PreparedStatement statement, final ResultSetExecutor<T> executor) {
        try (final ResultSet resultSet = statement.executeQuery()) {
            return executor.extractData(resultSet);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new ExecuteException(e);
        }
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}
