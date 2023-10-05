package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.support.JdbcAccessor;

public class JdbcTemplate extends JdbcAccessor implements JdbcOperations {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public JdbcTemplate(final DataSource dataSource) {
        setDataSource(dataSource);
    }

    public Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("커넥션을 가져올 수 없습니다.");
        }
    }

    @Override
    public <T> T execute(final StatementCallback<T> callback) throws DataAccessException {
        log.info("execute: {}", callback.getSql());
        try (final var connection = getConnection();
            final var preparedStatement = connection.createStatement()) {
            return callback.doInStatement(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    @Override
    public <T> T execute(final PreparedStatementCallback<T> preparedStatementCallback)
        throws DataAccessException {
        try (final Connection connection = getConnection();
            final PreparedStatement preparedStatement = createPreparedStatement(connection,
                                                                                preparedStatementCallback.getSql())) {
            return preparedStatementCallback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement createPreparedStatement(final Connection connection, final String sql)
        throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override
    public void execute(final String sql) throws DataAccessException {
        class ExecuteStatementCallback implements StatementCallback<Object> {

            @Override
            public Object doInStatement(final Statement statement) throws SQLException {
                statement.execute(sql);
                return null;
            }

            @Override
            public String getSql() {
                return sql;
            }
        }

        execute(new ExecuteStatementCallback());
    }

    @Override
    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... args)
        throws DataAccessException {
        final SingleRowMapperResultSetExtractor<T> extractor = new SingleRowMapperResultSetExtractor<>(
            rowMapper);

        return execute(new PreparedStatementCallback<>() {
            @Override
            public T doInPreparedStatement(final PreparedStatement preparedStatement) throws SQLException {
                setPreparedStatementObjects(args, preparedStatement);
                final ResultSet resultSet = preparedStatement.executeQuery();
                return extractor.extractData(resultSet);
            }

            @Override
            public String getSql() {
                return sql;
            }
        });
    }

    @Override
    public <T> T query(final String sql, final ResultSetExtractor<T> extractor) throws DataAccessException {
        class QueryStatementCallback implements StatementCallback<T> {

            @Override
            public T doInStatement(final Statement statement) throws SQLException {
                final ResultSet resultSet = statement.executeQuery(sql);

                return extractor.extractData(resultSet);
            }

            @Override
            public String getSql() {
                return sql;
            }
        }

        return execute(new QueryStatementCallback());
    }

    @Override
    public <T> T query(final String sql, final RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, new SingleRowMapperResultSetExtractor<>(rowMapper));
    }

    @Override
    public int update(final String sql, final Object... args) throws DataAccessException {
        return execute(new PreparedStatementCallback<>() {
            @Override
            public Integer doInPreparedStatement(final PreparedStatement preparedStatement) throws SQLException {
                setPreparedStatementObjects(args, preparedStatement);
                return preparedStatement.executeUpdate();
            }

            @Override
            public String getSql() {
                return sql;
            }
        });
    }

    private void setPreparedStatementObjects(final Object[] args, final PreparedStatement preparedStatement)
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
