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
        return update(sql, argumentsSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatementSetter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, argumentsSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                             final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatementSetter, preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, argumentsSetter(args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatementSetter, preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                final T result = rowMapper.mapRow(resultSet);
                if (resultSet.next()) {
                    throw new DataAccessException("조회 결과가 1건이어야 하지만 2건 이상입니다.");
                }
                return result;
            }
        });
    }

    private <T> T execute(final String sql,
                          final PreparedStatementSetter preparedStatementSetter,
                          final PreparedStatementCallback<T> action) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            preparedStatementSetter.setValues(preparedStatement);
            return action.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter argumentsSetter(final Object... args) {
        return preparedStatement -> {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
        };
    }
}
