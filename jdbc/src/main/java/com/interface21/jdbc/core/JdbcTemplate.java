package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return update(sql, statement -> bindParameters(statement, args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    /** 전달받은 Connection의 트랜잭션과 종료는 호출자가 관리한다. */
    public int update(final Connection connection, final String sql, final Object... args) {
        return execute(connection, sql, statement -> bindParameters(statement, args), PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, statement -> bindParameters(statement, args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return executeQuery(sql, setter, resultSet -> {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        });
    }

    /**
     * 조회 결과가 없으면 null을 반환하고, 두 건 이상이면 IncorrectResultSizeDataAccessException을 던진다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, statement -> bindParameters(statement, args), rowMapper);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return executeQuery(sql, setter, singleResultExtractor(sql, rowMapper));
    }

    /** 단건 조회 규칙을 따르며, 전달받은 Connection의 트랜잭션과 종료는 호출자가 관리한다. */
    public <T> T queryForObject(final Connection connection, final String sql,
                                final RowMapper<T> rowMapper, final Object... args) {
        return execute(connection, sql, statement -> bindParameters(statement, args),
                queryCallback(singleResultExtractor(sql, rowMapper)));
    }

    private <T> ResultSetExtractor<T> singleResultExtractor(final String sql, final RowMapper<T> rowMapper) {
        return resultSet -> {
            if (!resultSet.next()) {
                return null;
            }
            final T result = rowMapper.mapRow(resultSet);
            if (resultSet.next()) {
                throw new IncorrectResultSizeDataAccessException(
                        "Expected at most one row, but found more than one row. SQL: " + sql);
            }
            return result;
        };
    }

    private <T> T executeQuery(final String sql, final PreparedStatementSetter setter,
                               final ResultSetExtractor<T> extractor) {
        return execute(sql, setter, queryCallback(extractor));
    }

    private <T> PreparedStatementCallback<T> queryCallback(final ResultSetExtractor<T> extractor) {
        return statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                return extractor.extractData(resultSet);
            }
        };
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection()) {
            return execute(connection, sql, setter, callback);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private <T> T execute(final Connection connection, final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private void bindParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    private interface ResultSetExtractor<T> {

        T extractData(ResultSet resultSet) throws SQLException;
    }
}
