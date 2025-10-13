package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        execute(
                sql,
                preparedStatement -> {
                    preparedStatement.executeUpdate();
                    return null;
                },
                parameters
        );
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, rowMapper, parameters).stream()
                .findFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(
                sql,
                preparedStatement -> {
                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        return extractResults(resultSet, rowMapper);
                    }
                },
                parameters
        );
    }

    public <T> T query(final String sql, final ResultSetExtractor<T> resultSetExtractor, final Object... parameters) {
        return execute(
                sql,
                preparedStatement -> {
                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        return resultSetExtractor.extractData(resultSet);
                    }
                },
                parameters
        );
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            bindParameters(preparedStatement, parameters);
            return callback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void bindParameters(final PreparedStatement preparedStatement, final Object[] parameters) {
        try {
            for (int i = 0; i < parameters.length; i++) {
                final Object parameter = parameters[i];
                final int parameterIndex = i + 1;

                if (parameter == null) {
                    final ParameterMetaData metaData = preparedStatement.getParameterMetaData();
                    final int sqlType = metaData.getParameterType(parameterIndex);

                    preparedStatement.setNull(parameterIndex, sqlType);
                    continue;
                }

                preparedStatement.setObject(parameterIndex, parameter);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> extractResults(final ResultSet resultSet, final RowMapper<T> rowMapper) {
        try {
            final List<T> results = new ArrayList<>();

            int rowNumber = 0;
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, rowNumber++));
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
