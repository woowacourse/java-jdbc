package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, new ArgumentPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, new ArgumentPreparedStatementSetter(args), rowMapper);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter preparedStatementSetter,
                                final RowMapper<T> rowMapper) {
        return singleResult(query(sql, preparedStatementSetter, new RowMapperResultSetExtractor<>(rowMapper)));
    }

    public <T> T query(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final ResultSetExtractor<T> resultSetExtractor
    ) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSetExtractor.extractData(resultSet);
            }
        });
    }

    /** 트랜잭션에 바인딩된 연결이 있으면 그 연결로 실행하고, 그 연결의 커밋·롤백과 반환은 트랜잭션에 맡긴다. */
    private <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        log.debug("query : {}", sql);
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return action.doInPreparedStatement(preparedStatement);
        } catch (final SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private <T> T singleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.getFirst();
    }
}
