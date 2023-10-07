package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcAccessor;

public class JdbcTemplate extends JdbcAccessor implements JdbcOperations {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public JdbcTemplate(final DataSource dataSource) {
        setDataSource(dataSource);
    }

    public Connection getConnection() {
        return DataSourceUtils.getConnection(getDataSource());
    }

    @Override
    public <T> T execute(final StatementCallback<T> callback, final String sql) throws DataAccessException {
        log.info("execute: {}", sql);
        final var connection = getConnection();

        try (final var statement = connection.createStatement()) {
            return callback.doInStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            log.info("release connection: {}", sql);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }
    }

    private PreparedStatement createPreparedStatement(final Connection connection, final String sql)
        throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override
    public void execute(final String sql) throws DataAccessException {
        execute(statement -> {
            statement.execute(sql);
            return null;
        }, sql);
    }

    @Override
    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... args)
        throws DataAccessException {
        final SingleRowMapperResultSetExtractor<T> extractor = new SingleRowMapperResultSetExtractor<>(
            rowMapper);

        return execute(statement -> {
            final PreparedStatement preparedStatement = createPreparedStatement(statement.getConnection(), sql);
            setPreparedStatementObjects(preparedStatement, args);
            final ResultSet resultSet = preparedStatement.executeQuery();
            return extractor.extractData(resultSet);
        }, sql);
    }

    @Override
    public <T> T query(final String sql, final ResultSetExtractor<T> extractor) throws DataAccessException {
        return execute(statement -> {
            final ResultSet resultSet = statement.executeQuery(sql);
            return extractor.extractData(resultSet);
        }, sql);
    }

    @Override
    public <T> T query(final String sql, final RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, new SingleRowMapperResultSetExtractor<>(rowMapper));
    }

    @Override
    public int update(final String sql, final Object... args) throws DataAccessException {
        return execute(statement -> {
            final PreparedStatement preparedStatement = createPreparedStatement(statement.getConnection(), sql);
            setPreparedStatementObjects(preparedStatement, args);
            return preparedStatement.executeUpdate();
        }, sql);
    }

    private void setPreparedStatementObjects(final PreparedStatement preparedStatement, final Object... args)
        throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    @Override
    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }
}
