package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public int update(final String sql, final PreparedStatementSetter statementSetter) {
        return execute(sql, PreparedStatement::executeUpdate, statementSetter);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, statement -> bindParameters(statement, args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                             final PreparedStatementSetter statementSetter) {
        return executeQuery(sql, resultSet -> {
            final List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }

            return results;
        }, statementSetter);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, statement -> bindParameters(statement, args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final PreparedStatementSetter statementSetter) {
        return executeQuery(sql, resultSet -> {
            if (!resultSet.next()) {
                return null;
            }

            final T result = rowMapper.mapRow(resultSet);

            if (resultSet.next()) {
                throw new DataAccessException("단건 조회 결과는 최대 1개여야 합니다. 2개 이상의 행이 조회되었습니다.");
            }

            return result;
        }, statementSetter);
    }

    private <T> T executeQuery(final String sql, final ResultSetExtractor<T> resultSetExtractor,
                              final PreparedStatementSetter statementSetter) {
        return execute(sql, statement -> {
            try (final ResultSet resultSet = statement.executeQuery()) {
                return resultSetExtractor.extractData(resultSet);
            }
        }, statementSetter);
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback,
                         final PreparedStatementSetter statementSetter) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            statementSetter.setValues(statement);

            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        }
    }

    private void bindParameters(final PreparedStatement statement, final Object... args) throws SQLException {
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
