package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator();
    private final ConnectionManager connectionManager;

    public JdbcTemplate(final DataSource dataSource) {
        this.connectionManager = new ConnectionManager(dataSource);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(sql, preparedStatement -> SingleResult.convert(getQueryResult(rowMapper, preparedStatement)), args);
    }

    private <T> T executeQuery(final String sql, final SqlExecutor<T> executor, final Object... args) {
        final Connection connection = connectionManager.getConnection();
        try (final PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection, sql, args)) {
            return executor.execute(preparedStatement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            connectionManager.closeNotTransactional(connection);
        }
    }

    private <T> List<T> getQueryResult(
            final RowMapper<T> rowMapper,
            final PreparedStatement preparedStatement
    ) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        return executeQuery(sql, preparedStatement -> getQueryResult(rowMapper, preparedStatement), args);
    }

    public int execute(final String sql, final Object... args) {
        return executeQuery(sql, PreparedStatement::executeUpdate, args);
    }
}
